angular.module('controllers')
    .controller('ctrlManageSession', ['$http', '$location', 'SelectTrainingService','SelectSessionService', function ($http, $location, SelectTrainingService,SelectSessionService) {
        var self = this;
        self.listTrainingSession=[];

        self.training = SelectTrainingService.get();
        /*** Recupération les sessions **/
        $http.get("api/formations/" + self.training.id + "/sessions").then(function (data) {
            self.listTrainingSession=data.data;
        });

        self.redirectRegisterTrainingSession = function () {
            $location.url("/RegisterTrainingSession");
        };

        self.returnToRegisterTraining = function () {
            $location.url("/RegisterTraining");
        };

        self.redirectToSession=function (session) {
            SelectSessionService.select(session);
            $location.url("/ChangeRegisterTrainingSession");  
        };
    }])

    .config(['$routeProvider', function ($routeProvider) {
        $routeProvider
            .when('/ManageSession', {
                templateUrl: 'templates/manageSession.html',
                controller: 'ctrlManageSession',
                controllerAs: 'MS'
            })
    }
    ]);