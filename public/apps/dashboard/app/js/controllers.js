'use strict';

/* Auth */
function checkAuth($cookies, $rootScope, User) {
    if (null==$cookies.userId||null==$cookies.accessToken||1>$cookies.userId.length||1>$cookies.accessToken.length) {
    	location.replace("/login?redirect="+encodeURIComponent(location.href))
    	return false
    }
    $rootScope.checkLoggedIn=true
    if (null==$rootScope.user||$cookies.userId!=$rootScope.user.id) $rootScope.user=User.get()
    return true
}

/* Utils */
function formatTimestamp(timestamp) {
    var months=["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    var d = new Date(timestamp);
    var curr_date = d.getDate();
    var curr_month = d.getMonth() + 1; //Months are zero based
    var curr_year = d.getFullYear();
    return curr_date + " " + months[curr_month-1] + ". " + curr_year;
}

function createRef(refCookie) {
    if (null==refCookie) return ""
    return "?ref="+refCookie
}

function googleAnalytics($window, $location, $rootScope, $scope, $cookies) {
    $scope.$on('$viewContentLoaded', function(event) {
        //$window._gaq.push(['_trackPageView', '/dashboard'+$location.path()]);
        try {
            var pageTracker=$window._gat._getTracker("");
            pageTracker._trackPageview('/dashboard'+$location.path()+createRef($cookies.ref));
        } catch(err) {}
    });

    $rootScope.trackDownloadAndRedirect=function(url) {
        try {
            var pageTracker=$window._gat._getTracker("");
            pageTracker._trackEvent('Download', 'ZIP', url);
        } catch(err) {}
        setTimeout(function() { location.href=url }, 1000);
    }
}

/* Controllers */

// Empty Controller
function EmptyController($window, $location, $scope, $cookies, $rootScope, User) {
    if (!checkAuth($cookies, $rootScope, User)) return

}
EmptyController.$inject=['$window', '$location', '$scope', '$cookies', '$rootScope', 'User'];


// Apps Controller
function AppsController($window, $location, $scope, $cookies, $rootScope, User, App) {
	 if (!checkAuth($cookies, $rootScope, User)) return
    googleAnalytics($window, $location, $rootScope, $scope, $cookies)
    $scope.fetchedApps=false

	// Controller
	$scope.apps=App.query(function() {
        $scope.fetchedApps=true
    })
    $scope.hasApps=function() {
        if (!$scope.fetchedApps) return -1
        if (null==$scope.apps[0]) return 0
        else return 1
    }

    $scope.formatTimestamp=formatTimestamp
}
AppsController.$inject=['$window', '$location', '$scope', '$cookies', '$rootScope', 'User', 'App'];


// App Controller
function AppController($window, $location, $scope, $cookies, $rootScope, User, App, $routeParams) {
   	 if (!checkAuth($cookies, $rootScope, User)) return
    googleAnalytics($window, $location, $rootScope, $scope, $cookies)
    $scope.status=1

    // Controller

    $scope.formatTimestamp=formatTimestamp

    $scope.app=App.get({appId: $routeParams.appId}, function(app) {
//        $scope.mainImageUrl=phone.images[0];
    });

    $scope.save=function(isFormValid) {
        if (!isFormValid) { $scope.status=0; return }
        $scope.app.$save(function() {
            location.href="#/apps";
        }, function(result) {
            $scope.status=result.status
            if ($scope.status<100) $scope.status=503
        })
    }
    $scope.saveUsingForm=function(isFormValid) {
        if (!isFormValid) { $scope.status=0; return }
//        $('.test-file').change(function(){
//            var file = this.files[0];
//            var name = file.name;
//            var size = file.size;
//            var type = file.type;
//            //your validation
//        });
//
//        function progressHandlingFunction(e){
//            if(e.lengthComputable){
//                $('.test-progress').attr({value:e.loaded,max:e.total});
//            }
//        }
//
//        $('.test-button').click(function(){
//            var formData = new FormData($('.test-form')[0]);
//            $.ajax({
//                url: '/apps/2495/form?userId=3&accessToken=at11111',  //server script to process data
//                type: 'POST',
//                xhr: function() {  // custom xhr
//                    var myXhr = $.ajaxSettings.xhr();
//                    if(myXhr.upload){ // check if upload property exists
//                        myXhr.upload.addEventListener('progress',progressHandlingFunction, false); // for handling the progress of the upload
//                    }
//                    return myXhr;
//                },
//                //Ajax events
//                //beforeSend: beforeSendHandler,
//                //success: completeHandler,
//                //error: errorHandler,
//                // Form data
//                data: formData,
//                //Options to tell JQuery not to process data or worry about content-type
//                cache: false,
//                contentType: false,
//                processData: false
//            });
//        });
        var form=document.getElementById("cert-form")
        var formData=new FormData(form)
        $.ajax({
            url: "/apps/"+$scope.app.id+"/form?userId="+$cookies.userId+"&accessToken="+$cookies.accessToken,
            type: "PUT",
            data: formData,
            success: function() { location.href="#/apps" },
            error: function(result) {
                $scope.status=result.staus
                if (100>$scope.status) $scope.status=503
            },
            processData: false,  // tell jQuery not to process the data
            contentType: false   // tell jQuery not to set contentType
        })
    }


}
AppController.$inject=['$window', '$location', '$scope', '$cookies', '$rootScope', 'User', 'App', '$routeParams'];


// New App Controller
function NewAppController($window, $location, $scope, $cookies, $rootScope, User, App) {
    if (!checkAuth($cookies, $rootScope, User)) return
    googleAnalytics($window, $location, $rootScope, $scope, $cookies)
    $scope.status=1;

    // Controller
    $scope.app={}
    $scope.create=function(isFormValid) {
        if (!isFormValid) { $scope.status=0; return }
        var appInfo={"name":$scope.app.name}
        if (null!=$scope.app.downloadUrl) appInfo["downloadUrl"]=$scope.app.downloadUrl
        var newApp=App.create(appInfo, function() {
            $scope.staus=200
            location.href="#/apps/"+newApp.id+"/first"
        }, function(result) {
            $scope.status=result.status
            if ($scope.status<100) $scope.status=503
        })
    }
}
NewAppController.$inject=['$window', '$location', '$scope', '$cookies', '$rootScope', 'User', 'App'];

// Account Controller
function AccountController($window, $location, $scope, $cookies, $rootScope, User, $http) {
   	 if (!checkAuth($cookies, $rootScope, User)) return
    googleAnalytics($window, $location, $rootScope, $scope, $cookies)

    $scope.password={"old":"", "new1":"", "new2":""}
    $scope.status=1;

    $scope.changePassword=function(isFormValid) {
        if (!isFormValid) { $scope.status=0; return }
        if (null==$scope.password.old) $scope.password.old=""
        if (null==$scope.password.new1) $scope.password.new1=""
        if (null==$scope.password.new2) $scope.password.new2=""

        if ($scope.password.new1.length<6||$scope.password.new1!=$scope.password.new2) { $scope.status=0; return }
        $http.put('/users/'+$cookies.userId+'/password?currentPassword='+encodeURIComponent($scope.password.old), $scope.password.new1).success(function(data, status) {
            location.href="#/account"
        }).error(function(data, status) {
                $scope.status=status
                if ($scope.status<100) $scope.status=503
        })
    };

}
AccountController.$inject=['$window', '$location', '$scope', '$cookies', '$rootScope', 'User', '$http'];


