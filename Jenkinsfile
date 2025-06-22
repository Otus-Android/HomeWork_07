pipeline {
    agent {
        dockerfile true
    }
    parameters {
        booleanParam(name: 'RUN_PROFILER', defaultValue: false, description: 'Запустить gradle-profiler для анализа производительности сборки?')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Grant execution permission') {
            steps {
                sh 'chmod +x gradlew'
            }
        }

        stage('Build') {
            steps {
                script {
                    if (env.BRANCH_NAME == 'master') {
                        echo "Building for master branch..."
                        sh './gradlew assembleRelease'
                    } else {
                        echo "Building for branch ${env.BRANCH_NAME}..."
                        sh './gradlew assembleDebug'
                    }
                }
            }
        }

        stage('Unit Tests') {
            steps {
                sh './gradlew test'
            }
        }
        
        stage('Static Analysis') {
            steps {
                sh './gradlew lint'
            }
        }

        stage('Profiling') {
            when {
                // Запускать только если параметр RUN_PROFILER установлен в true
                expression { params.RUN_PROFILER }
            }
            steps {
                script {
                    echo "Starting Gradle profiler..."
                    sh "jbang dev.gradle.profiler.tool.Main --benchmark --project-dir . --scenario-file profiler.scenarios"
                    archiveArtifacts artifacts: 'profile-out/', followSymlinks: false
                }
            }
        }
    }

    post {
        always {
            echo 'Build finished.'
            cleanWs()
        }
    }
} 