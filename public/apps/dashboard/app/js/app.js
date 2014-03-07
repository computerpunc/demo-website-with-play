'use strict';


// Declare app level module which depends on filters, and services
angular.module('myApp', ['ngCookies', 'myAppFilters', 'myAppServices', 'myAppDirectives']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/apps', {templateUrl: 'dashboard/partials/apps.html', controller: AppsController})
    $routeProvider.when('/apps/new', {templateUrl: 'dashboard/partials/app-new.html', controller: NewAppController})
    $routeProvider.when('/apps/:appId/settings', {templateUrl: 'dashboard/partials/app-settings.html', controller: AppController})
    $routeProvider.when('/apps/:appId', {templateUrl: 'dashboard/partials/app.html', controller: AppController})
    $routeProvider.when('/apps/:appId/first', {templateUrl: 'dashboard/partials/app-first.html', controller: AppController})
    $routeProvider.when('/account', {templateUrl: 'dashboard/partials/account.html', controller: AccountController})
    $routeProvider.when('/account/password', {templateUrl: 'dashboard/partials/account-change-password.html', controller: AccountController})
    $routeProvider.otherwise({redirectTo: '/apps'});
  }]).
  run(['$rootScope', function($rootScope) {
     $rootScope.config=angularJsConfig
  }]);
