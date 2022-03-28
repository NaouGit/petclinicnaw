node {
  stage ('SCM Checkout'){
    git credentialId: 'git-creds', url: 'https://github.com/NaouGit/petclinicnaw.git'
  }  
  stage ('Sonar Scan'){
    withSonarQubeEnv('sonarqube'){
      sh 'mvn clean package sonar:sonar'
          }
      }
  
  stage('Quality Gate'){
  timeout(time: 1, unit: 'HOURS') { // Just in case something goes wrong, pipeline will be killed after a timeout
    def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
    if (qg.status != 'OK') {
      error "Pipeline aborted due to quality gate failure: ${qg.status}"
    }
  }
}
 stage ('Mvn Package'){
    sh 'mvn clean package'
  }
  stage ('Build Docker Image'){
   sh 'docker build -t naoudock/petclinic .'
   sh 'docker tag naoudock/petclinic:latest ${ACR_LOGINSERVER}/petclinic'
  }
  stage ('Docker Push'){
    withCredentials([string(credentialsId: 'DOCKER_HUB_CREDENTIALS', variable: 'DOCKER_HUB_CREDENTIALS')]) {
    // some block
        sh 'docker login -u naoudock -p ${DOCKER_HUB_CREDENTIALS}'
    }
        sh 'docker push naoudock/petclinic '
  }
stage ('ACR Login'){
 sh 'docker login ${ACR_LOGINSERVER} -u Petclinicnawk8sCR -p gjN=l997jCdrBMy=tGG8EsLMM7kD+BNX'
 sh 'docker push ${ACR_LOGINSERVER}/petclinic'
}
}
stage ('K8S Deploy'){
    steps{
        script{
            kubernetesDeploy(
                configs:'k8s-deployment.yaml',
                kubeconfigId:'K8S',
                enableConfigSubstition: true
                )
        }
    }
}
