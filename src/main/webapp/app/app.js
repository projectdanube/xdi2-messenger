'use strict';

var CloudMessengerApp = angular.module("CloudMessengerApp", [
    "ui.bootstrap",
    "ngResource",
    /*"ngWebSocket",*/
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
                    if (angular.isString(response.data) && response.data.indexOf('4d0bafeb-5b4c-4cbb-865e-d6211de5174e-login') !== -1)
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


controllers.controller("ConfigurationCtrl", ['$scope', '$http', 'Notification', 'AuthorizedSender',
  function ($scope, $http, Notification, AuthorizedSender) {

        var refreshAuthorizedSenders = function () {

            $scope.inProgress = true;
            AuthorizedSender.query(function (data) {
                $scope.authorizedSenders = data;
                $scope.inProgress = false;
            }, function (error) {
                Notification.show('Something went wrong while getting the list of authorized senders, please try again.', {
                    type: "danger"
                });
                $scope.inProgress = false;
            });
        };

        $scope.addSender = function (cloudName) {

            AuthorizedSender.save(cloudName, function (data) {
                refreshAuthorizedSenders();
                Notification.show(cloudName + ' added as an authorized sender', {
                    type: "success"
                });
            }, function (data) {
                Notification.show('Error adding ' + cloudName + ' as an authorized sender!', {
                    type: "danger"
                });
            });
        };

        $scope.deleteSender = function (cloudName) {

            AuthorizedSender.delete({
                cloudName: cloudName
            }, function (data) {
                refreshAuthorizedSenders();
                Notification.show(cloudName + ' deleted from your authorized senders list', {
                    type: "success"
                });
            }, function (data) {
                Notification.show('Error deleting ' + cloudName + ' from your authorized senders list!', {
                    type: "danger"
                });

            });
        };

        refreshAuthorizedSenders();

  }]);

controllers.controller("MessengerCtrl", ['$scope', 'Message', 'Notification',
  function ($scope, Message, Notification) {

        $scope.view = '';

        var refreshMessages = function () {

            $scope.inProgress = true;
            Message.query(function (data) {

                // shouldnt show the message the first refresh
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

        $scope.authorizedSenders = function () {
            $scope.view = 'authorizedSenders';
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

                if (data.data.indexOf('Link contract violation') >= 0 || data.data.indexOf('No link contract') >= 0) {
                    errorMsg = 'Destination cloud does not accept messages from you. Please contact its owner to add you as an authorized sender.';
                }

                Notification.show(errorMsg, {
                    type: "danger"
                });
            });
        };

        setInterval(refreshMessages, 15000);

        refreshMessages();
  }]);




var services = angular.module('CloudMessengerApp.services', ['ngResource']);

services.factory('Message', ['$resource',
  function ($resource) {
        return $resource('api/1.0/messenger/messages/:id', {
            id: '@id'
        });
  }]);

services.factory('AuthorizedSender', ['$resource',
  function ($resource) {
        return $resource('api/1.0/messenger/authorizedsenders/:cloudName', {
            cloudName: '@cloudName'
        });
  }]);

/*services.factory('XdiWs', ['$websocket',
  function ($websocket) {
        // Open a WebSocket connection
        var dataStream = $websocket('wss://website.com/data');

        var collection = [];

        dataStream.onMessage(function (message) {
            collection.push(JSON.parse(message.data));
        });

        var methods = {
            collection: collection,
            get: function () {
                dataStream.send(JSON.stringify({
                    action: 'get'
                }));
            }
        };

        return methods;
  }]);*/

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