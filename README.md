# TCC-APP

Projeto de conclusão de curso

# Utilidades

- Comando DockerFile para criar imagem da aplicação:

  > docker build --pull --rm -f "Dockerfile" -t tccapp:latest "."

- Comando para usar Docker-compose e subir imagem da aplicação e mongodb em containers e utilizar juntos:
  > docker compose -f "docker-compose.yml" up -d --build

OBS:

--net host -->TROCAR NETWORK DOCKER

--docker run -v input:/opt/app/input -v output:/opt/app/output --rm -it -p 8080:8080/tcp tccapp:latest --> INICIAR CONTAINER DA APLICAÇÃO COM INSERÇÃO DE VOLUMES INPUT E OUTPUT QUE O PROJETO NECESSITA.

# Config VS-CODE:

Configurações para rodar projeto no StandAlone:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "TccAppApplication",
      "request": "launch",
      "mainClass": "com.uninter.tcc.TccAppApplication",
      "projectName": "tcc-app",
      "env": {
        "MONGODB_HOST": "NUMERO DO IP - UTILIZADO: IP DA MAQUINA OU LOCALHOST,DEPENDENDO        ONDE O MONGO ESTA SENDO EXECUTADO",
        "MONGODB_PORT": "NUMERO DA PORTA - UTILIZADO: 27017",
        "MONGODB_DATABASE": " DATABASE DO MONGO - UTILIZADO:AnalysisData",
        "KAFKA_BOOTSTRAP_SERVERS": "192.168.100.111:29093",
        "KAFKA_GROUP_ID": 1,
        "THREAD_POOL": 10
      }
    }
  ]
}
```

#
