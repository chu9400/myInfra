# MyInfra 프로젝트
이 프로젝트는 Docker 기반의 컨테이너 환경에서 네트워크 설계 및 통신을 학습하기 위해 설계되었습니다.

가상 환경에서 서버 간의 통신 흐름을 시뮬레이션하여 인프라 설계와 운영에 대한 실무 경험을 쌓는 것을 목표로 합니다. 

<hr />



## 프로젝트 설계도
  ![myinfra.jpg](readme_img%2Fmyinfra.jpg)

- 외부 요청이 Host PC의 80번 포트로 전달됩니다.
- Nginx 컨테이너는 이를 "app_network"를 통해 Spring Boot 웹 서버(8080번 포트)로 프록시합니다.
- Spring Boot는 "db_network"를 통해 MySQL 데이터베이스(3306번 포트)와 통신합니다.
- MySQL이 데이터를 처리한 후 결과를 Spring Boot로 반환합니다.
- Spring Boot는 최종 응답을 Nginx로 전달하며, Nginx는 이를 클라이언트에게 반환합니다.


<hr />


## 프로젝트 주요 목표
### 1. 컨테이너 네트워크 통신 구현
- Docker 네트워크를 활용해 서버 간의 안정적인 통신 구조를 설계합니다.

### 2.리버스 프록시 이해 및 구현
- Nginx를 통해 클라이언트 요청을 백엔드 서버로 라우팅하며, 리버스 프록시의 원리를 학습합니다.

### 3. 데이터 흐름 이해 및 분석
- 클라이언트 요청부터 응답까지의 전체 과정을 시뮬레이션하여 네트워크 및 데이터베이스 통신의 구조를 파악합니다.


<hr />


## 사용 기술 스택
- Docker
- Docker Compose 
- Java Spring Boot (JPA, Thymeleaf)
- MySQL 
- Nginx


<hr />


## 프로젝트 실행 방법
### 1. 환경 요구 사항
- Docker Desktop 설치


### 2. 프로젝트 실행
- GitHub에서 프로젝트를 다운로드하여 압축을 풉니다.
- 터미널에서 프로젝트 폴더로 이동합니다.
  - 이미지 빌드:
    - mysql
      - ```shell
        docker pull mysql:8.0.40
        ```

    - Nginx
      - ```shell
        docker build -t img_nginx:v1 ./nginx
        ```
- Docker Compose로 컨테이너 실행:
  - ```docker compose up --build```

-  브라우저에서 http://localhost:80으로 접속하여 애플리케이션이 정상적으로 작동하는지 확인합니다.
![homepage.png](readme_img%2Fhomepage.png)


<hr />

## 프로젝트 구조 및 설명
### 1. 리버스 프록시 서버
- Nginx로 구성된 리버스 프록시 서버입니다.
- 기본 설정 충돌 문제를 방지하기 위해 Nginx 기본 설정(default.conf)을 삭제하고 새로 구성했습니다.
- myconfig.conf 파일을 생성하여, 80번 포트로 들어온 요청을 "웹 서버(8080 포트)"로 전달하도록 설정했습니다.


### 2. 웹 서버
- Spring Boot와 JPA를 사용해 구성되었습니다.
- 각 기능을 서비스 레이어로 분리하여 구조화했습니다.
- 가상의 쇼핑몰 환경에서 CRUD 기능을 통해 데이터베이스와의 통신을 확인할 수 있습니다.

### 3. DB 서버
- MySQL로 구성된 데이터베이스 서버입니다.
- init.sql을 작성하여 초기 db 세팅을 입력하여 어느 곳에서든 같은 db 환경을 제공합니다.
- Docker 환경에서 root 계정 대신 별도의 사용자 계정을 생성하여 보안을 강화 했습니다.

### 4. 네트워크
- 각 서버에 대해 네트워크 인터페이스를 생성하여, 필요한 서버들만을 네트워크로 묶어 보안을 강화했습니다.
- 아래 코드는 Docker Compose의 네트워크 설정 부분을 나타냅니다 
    ```
    networks:
        app_network: # 리버스 프록시 서버와 웹 서버 간의 통신을 위한 네트워크 
        db_network: # 웹 서버와 DB 서버 간의 통신을 위한 네트워크
    ```

<hr />

## 오류 노트 및 최적화 기록
### MySQL 계정 권한 오류
- root 계정으로 접속 시 권한 문제로 인해 데이터베이스와의 통신이 실패하는 경우가 있었습니다.
- MySQL은 root 계정을 기본적으로 외부 접속에 제한하기 때문에, 새 계정을 생성하여 문제를 해결했습니다.
- 해결 코드:
  ```sql
  CREATE USER 'newuser'@'%' IDENTIFIED BY 'qwer1234';
  GRANT ALL PRIVILEGES ON *.* TO 'newuser'@'%' WITH GRANT OPTION;
  FLUSH PRIVILEGES;
  ```

<hr />

### Docker 내 DB URL 문제
- Docker 환경에서는 컨테이너 이름을 데이터베이스 URL로 사용해야 함을 학습했습니다:
- 해결 코드:
  ```properties
  spring.datasource.url=jdbc:mysql://mysql-container:3306/myinfradb
  ```

<hr />  

### Graceful Shutdown 적용
- 웹 서버
    - SIGTERM 및 SIGINT 신호가 발생했을 때 애플리케이션이 종료되기 전에 특정 동작을 수행하도록 설정했습니다.
       ```java
       @PreDestroy
       public void onShutdown() {
            System.out.println("애플리케이션 종료 중...");
       }
      ```

- Nginx 서버
    - Docker Compose로 컨테이너를 종료할 때 Nginx가 약 10초가량 지연되는 문제가 있었습니다. 이를 해결하기 위해 **stop_grace_period**를 설정했습니다.
       ```yaml
       nginx-container:
          stop_grace_period: 1s  # 종료 명령 후 1초 대기 후 강제 종료
      ```

<hr />

### 파일 시스템 권한 오류 (Windows → Docker)

#### 문제 상황 
- Github에 push 후 Docker 명령어 실행 중 아래와 같은 권한 오류가 발생했습니다:
  ```shell
  ERROR [spring-boot-container internal] load build context
  failed to solve: archive/tar: unknown file mode ?rwxr-xr-x
  ```
  
- Github에 push하는 과정에서 작업 PC의 NTFS 파일 시스템에 저장된 파일의 권한 정보가 변경되었습니다. 
- 이로 인해 Windows PowerShell 환경에서는 Docker가 프로젝트 폴더에 접근할 수 없게 되었습니다.



#### 해결 방법
- WSL2 설치 및 사용 
  - Windows Subsystem for Linux 2(WSL2)를 설치하여 Docker 명령어를 WSL2 환경에서 실행했습니다.
  WSL2는 리눅스 환경으로 Docker를 실행하므로, 리눅스 기반의 Docker 작업이 원활히 이루어졌습니다.
  - 아래 명령어로 폴더에 권한을 부여할 수도 있습니다.
    ```shell
      chmod -R 755 ./project-folder
    ```

<hr />

## 추후 개선 사항
- 보안 강화
  - Nginx SSL 인증서 추가로 HTTPS 통신 지원
- 확장성
  - 다수의 webServer 생성 후 리버스 프록시 서버로 로드밸런싱 구현 