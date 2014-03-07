@()

//angular.module('config', []).
//  run(['$rootScope', function($rootScope) {
//    alert("from config")
//
//    //$rootScope.config=@@config
//  }]);

angularJsConfig=@JavaScript(play.api.Play.current.configuration.getString("controllers.angularjs.config").get)//{"servers":{"www":"http://localhost:9002"}}