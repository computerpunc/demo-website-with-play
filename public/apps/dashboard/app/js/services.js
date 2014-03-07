'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('myAppServices', ['ngResource', 'ngCookies']).
    factory('App', function($resource, $cookies){
        return $resource('/apps/:appId', { 'userId':$cookies.userId, 'accessToken':$cookies.accessToken, 'appId':'@id' }, {
            query: { method:'GET', isArray:true },
            create: { method:'POST' },
            save: { method:'PUT' }
        })
    }).
    factory('User', function($resource, $cookies){
        return $resource('/users/'+$cookies.userId+'?accessToken='+$cookies.accessToken, {}, {
            //query: { method:'GET', params:{ 'userId':$cookies.userId, 'accessToken':$cookies.accessToken }, isArray:true }
        })
    })