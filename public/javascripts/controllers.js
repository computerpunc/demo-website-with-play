function trackResponse(status, $cookies) {
    try {
        var pageTracker=window._gat._getTracker("");
        var ref=(null==$cookies.ref)?"":"&ref="+$cookies.ref
        pageTracker._trackPageview(location.pathname+((-1==location.href.indexOf("?"))?"?":"&")+"status="+status+ref);
    } catch(err) {}
}

function doLogin($scope, $http, email, password, redirect) {
   $http.get('/users?email='+encodeURIComponent(email)+'&password='+encodeURIComponent(password)).success(function(data, status) {
        $scope.status=status
        if (200!=status) return
        var users=angular.fromJson(data)
        if (null==users) { $scope.status=501; return }
        var user=users[0]
        if (null==user) { $scope.status=403; return }
        if (null==user.id) { $scope.status=501; return }
        if (null==user.accessToken) { $scope.status=501; return }
        document.cookie="userId="+user.id+"; path=/";
        document.cookie="accessToken="+user.accessToken+"; path=/";;
        location.href=(null!=redirect&&""!=redirect)?redirect:"/"
   	}).error(function(data, status) {
   		$scope.status=status
   		if ($scope.status<100) $scope.status=503
   		if (isToRedirectOnError) location.href=(null!=redirect&&""!=redirect)?redirect:"/"
   	});
}

function LoginController($scope, $http, $cookies) {
  $scope.status=1;
  $scope.user={"email":"", "password":""}
  $scope.login=function(isFormValid, redirect) {
    if (!isFormValid) { $scope.status=0; return }
       doLogin($scope, $http, $scope.user.email, $scope.user.password, redirect)
  };
}

function RegisterUserController($scope, $http, $cookies) {
  $scope.request={}
  $scope.status=1
  $scope.register=function(isFormValid, redirect) {
      if (!isFormValid) { $scope.status=0; return }
    $http.post('/users', $scope.request).success(function(data, status) {
		trackResponse(200, $cookies)
		$scope.status=200;
	}).error(function(data, status) {
        trackResponse(status, $cookies)
		$scope.status=status
        if ($scope.status<100) $scope.status=503
	});
  };
}

function ResetPasswordController($scope, $http, $cookies) {
  $scope.status=1;
  $scope.password={"new1":"", "new2":""}

  $scope.resetPassword=function(isFormValid, userId, email, token) {
    if (null==$scope.password.new1) $scope.password.new1=""
    if (null==$scope.password.new2) $scope.password.new2=""
    if (!isFormValid||$scope.password.new1!=$scope.password.new2||$scope.password.new1.length<6) { $scope.status=0; return }
    if ($scope.password.new1!=$scope.password.new2) return
    $http.put('/users/'+userId+'/password?resetPasswordToken='+token, $scope.password.new1).success(function(data, status) {
        trackResponse(200, $cookies)
		doLogin($scope, $http, email, $scope.password.new1, "/")
	}).error(function(data, status) {
        trackResponse(status, $cookies)
		$scope.status=status;
		if ($scope.status<100) $scope.status=503
	});
  };
}