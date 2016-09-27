angular.module('authentication', ['angular-jwt'])
    .service('currentUserService', ['$http', 'jwtHelper', '$location', '$q', function ($http, jwtHelper, $location, $q) {
        var self = this;
        return {
            decodeThisToken: function (userToken) {
                var tokenPayload = jwtHelper.decodeToken(userToken.data['userConnected']);
                this.setUserData(userToken.data['userConnected'], tokenPayload['sub'], tokenPayload['lastName'], tokenPayload['roles'], tokenPayload['id']);
            },
            disconnectCurrentUser: function () {
                var self = this;
                $http.post("api/userdisconnect", this.getUserToken()).then(function () {
                    self.setUserData(undefined, '', '', '', '')
                }).catch(function () {
                    console.log("Error !! User is not connected");
                })
            },
            setUserData: function (userToken, newFirstName, newLastName, newUserRole, newUserId) {
                self.token = userToken;
                self.firstName = newFirstName;
                self.lastName = newLastName;
                self.roles = newUserRole;
                self.userId = newUserId;
            },
            getUserToken: function () {
                return self.token;
            },
            getUserFirstName: function () {
                return self.firstName;
            },
            getUserLastName: function () {
                return self.lastName;
            },
            getUserRole: function () {
                return self.roles;
            },
            getCollaboratorIdentity: function () {
                return {id: self.userId, firstName: self.firstName, lastName: self.lastName};
            },
            isUserConnected: function () {
                return self.token
            },
            checkThisUserRole: function (userToken) {
                return $http.post('api/getuserrole', userToken).success(function (response) {
                    if (response === false) {
                        $location.url('/authentication.html');
                        return false;
                    }
                    else return true;
                })
            },
            checkIsAdminConnected: function () {
                var deffered = $q.defer();
                var userToken = this.getUserToken();
                if ((userToken !== undefined) && (this.checkThisUserRole(userToken))) {
                    deffered.resolve("Success")
                }
                else {
                    deffered.reject("Error");
                    $location.url('/authentication.html');
                }
                return deffered.promise;
            },
            checkIsCollaboratorConnected: function () {
                var deffered = $q.defer();
                var userToken = this.getUserToken();
                if ((userToken !== undefined) && (this.checkThisUserRole(userToken) === false)) {
                    deffered.resolve("Success")
                }
                else {
                    deffered.reject("Error");
                    $location.url('/authentication.html');
                }
                return deffered.promise;
            }
        };
    }]);