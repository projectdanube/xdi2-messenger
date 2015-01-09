'use strict';

var CloudMessengerApp = angular.module("CloudMessengerApp", [
    "ui.bootstrap",
    "ngResource",
    "CloudMessengerApp.controllers",
    "CloudMessengerApp.services",
    "CloudMessengerApp.directives"
]);


CloudMessengerApp.config(['$resourceProvider', '$httpProvider',
  function ($resourceProvider, $httpProvider) {

        // Don't strip trailing slashes from calculated URLs
        $resourceProvider.defaults.stripTrailingSlashes = false;

        // Handle with session expiration, forces user to login again.
        $httpProvider.interceptors.push(function ($q, $window) {
            return {
                'response': function (response) {
                    //this string should be in login.jsp
                    if (angular.isString(response.data) && response.data.indexOf('4d0bafeb-5b4c-4cbb-865e-d6211de5174e-login') != -1)
                        $window.location.reload();
                    return response;
                }
            };
        });

}]);


var controllers = angular.module("CloudMessengerApp.controllers", []);

controllers.controller("DefaultCtrl", ['$scope', '$rootScope', '$http',
  function ($scope, $rootScope, $http) {

        $rootScope.sessionProperties = {
            "cloudName": "",
            "cloudNumber": "",
            "xdiEndpointUrl": "",
            "environment": ""
        };
        $http.get('api/1.0/session/properties/')
            .success(function (data) {
                $rootScope.sessionProperties = data;
            })
            .error(function (data) {
                $rootScope.sessionProperties = {};
            });

  }]);


controllers.controller("ConfigurationCtrl", ['$scope', '$http', 'Notification',
  function ($scope, $http, Notification) {

        var checkSetup = function () {
            $http.get('api/1.0/messenger/setup/')
                .success(function (data) {
                    $scope.configuration = data;
                })
                .error(function (data) {
                    $scope.configuration = null;
                });
        };

        $scope.setup = function () {

            $http.post('api/1.0/messenger/setup/')
                .success(function (data) {
                    checkSetup();

                    Notification.show('Cloud successfully configured!', {
                        type: "success"
                    });
                })
                .error(function (data) {
                    checkSetup();

                    Notification.show('Error while configuring the cloud!', {
                        type: "danger"
                    });
                });

        };

        checkSetup();

  }]);

controllers.controller("MessengerCtrl", ['$scope', 'Message', 'Notification',
  function ($scope, Message, Notification) {

        $scope.view = '';

        var refreshMessages = function () {

            $scope.inProgress = true;
            Message.query(function (data) {

                if (_.size(data) > _.size($scope.messages) && _.isUndefined($scope.messages) === false) {
                    Notification.show('You\'ve got mail!');
                }

                $scope.messages = data;
                $scope.inProgress = false;
            }, function (error) {
                $scope.error = "Something went wrong while getting your messages, please try again.";
                $scope.inProgress = false;
            });

        };

        $scope.readMessage = function (id) {
            $scope.view = 'readMessage';
            $scope.currentMessage = _.find($scope.messages, function (m) {
                return m.xdiAddress === id;
            });
        };

        $scope.deleteMessage = function (id) {
            Message.delete({
                id: id
            }, function (data) {
                Notification.show('Message deleted successfully!', {
                    type: "success"
                });
                refreshMessages();
                $scope.view = '';
            }, function (error) {
                Notification.show('Error deleting the message!', {
                    type: "danger"
                });
            });
        };

        var composeMessage = function (to) {
            $scope.view = 'composeMessage';
            $scope.newMessage = {
                'to': to,
                'content': ''
            };

        };

        $scope.replyMessage = function (to) {
            composeMessage(to);
        };

        $scope.composeMessage = function () {
            composeMessage(null);
        };

        $scope.sendMessage = function () {

            Message.save($scope.newMessage, function (data) {
                $scope.view = '';
                Notification.show('Message sent successfully!', {
                    type: "success"
                });
            }, function (data) {

                var errorMsg = 'Error sending the message!';

                if (data.data.indexOf('Link contract violation') >= 0) {
                    errorMsg = 'It seems that the destination cloud in not yet configured.';
                }

                Notification.show(errorMsg, {
                    type: "danger"
                });
            });
        };

        setInterval(refreshMessages, 5000);

        refreshMessages();
  }]);




var services = angular.module('CloudMessengerApp.services', ['ngResource']);

services.factory('Message', ['$resource',
  function ($resource) {
        return $resource('api/1.0/messenger/messages/:id', {
            id: '@id'
        });
  }]);


services.factory('Notification', [

  function () {

        $.growl(false, {
            /*animate: {
                enter: 'animated bounceIn',
                exit: 'animated bounceOut'
            },*/
            offset: {
                x: 20,
                y: 75
            }
        });

        return {
            show: function (message, options) {
                return $.growl(message, options);
            }
        };
  }]);

var directives = angular.module("CloudMessengerApp.directives", []);

directives.directive('cloudNameValidation', ['$http', '$q',
    function ($http, $q) {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, elm, attrs, ngModel) {
                ngModel.$asyncValidators.cloudNameValidation = function (modelValue, viewValue) {
                    if (ngModel.$isEmpty(modelValue)) {
                        // consider empty models to be invalid
                        return $q.when(false);
                    }

                    var deferred = $q.defer();
                    $http.get('api/1.0/discovery/' + viewValue.trim())
                        .success(function (data) {
                            if (data.indexOf("uuid") < 0) {
                                deferred.reject("Cloud name doesn't exist.");
                            }
                            deferred.resolve();
                        })
                        .error(function (data) {
                            deferred.reject("server error");
                            deferred.resolve();
                        });

                    return deferred.promise;

                };
            }
        };
}]);