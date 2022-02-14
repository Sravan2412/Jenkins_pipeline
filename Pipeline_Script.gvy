pipeline{
    agent any
    // environment {
    //     PATH = "$PATH:C:/Work/apache-maven-3.6.3-bin/apache-maven-3.6.3/bin"
    // }
    stages{
       stage('GetCode'){
            steps{
                git 'https://github.com/ravdy/javaloginapp.git'
            }
            }        
       stage('Build'){
            steps{
                bat 'mvn clean package'
            }
            }
        stage('SonarQube analysis') {
//      def scannerHome = tool 'SonarScanner 4.0';
        steps{
        withSonarQubeEnv('sonarqube') { 
        // If you have configured more than one global server connection, you can specify its name
//      sh "${scannerHome}/bin/sonar-scanner"
        bat "mvn sonar:sonar"
            }
            }
            }
        stage ('Server'){
            steps {
               rtServer (
                  id: "jfrog", url: 'https://saravanan2492.jfrog.io/artifactory', username: 'saradhan24@gmail.com',
                  password: 'Saravanan@1sir', bypassProxy: true, timeout: 300)
            }
            }
        stage('Upload'){
            steps{
                rtUpload (
                 serverId:"jfrog" ,
                  spec: '''{
                   "files": [
                      {
                      "pattern": "*.war",
                      "target": "saravanan-snapshot-generic-local"
                      }
                            ]
                           }''',
                        )
            }
        }
        stage ('Publish build info') {
            steps {
                rtPublishBuildInfo (
                    serverId: "jfrog"
                )
            }
        }
        stage ('Deploy on this Server') {
            steps {
            deploy adapters: [tomcat9(credentialsId: 'tomcat-admin', path: '', url: 'http://localhost:8081')], contextPath: null, war: '**/*.war'
            }
        }
}
}

