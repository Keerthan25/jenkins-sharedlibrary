def call(String registryCred = 'a', String registryname = 'a', String docTag = 'a', String grepo = 'a', String gbranch = 'a', String gitcred = 'a', String depname = 'a', String contname = 'a', file k8scred = 'a') { 
//String 
pipeline {
environment { 
		registryCredential = "${registryCred}"
    		registry = "${registryname}" 	
		dockerTag = "${docTag}$BUILD_NUMBER"
		gitRepo = "${grepo}"
		gitBranch = "${gbranch}"
		gitCredId = "${gitcred}"
    		deployment = "${depname}"
    		containerName = "${contname}"
		kubernetesConfig = "${k8scred}"
	}
		
    agent none

    stages {
        stage("POLL SCM"){
		        agent{label 'docker'}
            steps {
             	checkout([$class: 'GitSCM', branches: [[name: "$gitBranch"]], extensions: [], userRemoteConfigs: [[credentialsId: "$gitCredId", url: "$gitRepo"]]])             
            }
        } 
        
        stage('BUILD IMAGE') {
		      agent{label 'docker'}
       		steps {
           	sh 'docker build -t $registry:$dockerTag .'             
          }
        }
        
        stage('PUSH HUB') { 
		      agent{label 'docker'}
          steps {
			      sh 'docker push $registry:$dockerTag'                   	
          }    
        }
        
        stage('DEPLOY IMAGE') {
		      steps {
			      
			      withKubeConfig[file(credentialsId:'kubernetesConfig', serverUrl: '')]  {
                sh 'kubectl set image deploy $deployment $containerName="$registry:$dockerTag" --record'
              
            }
		      }
	      }  
    }
}  
}
