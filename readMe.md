# MyInfra 프로젝트
- MyInfra는 Docker를 활용한 컨테이너 기반 네트워크 통신 학습을 목표로 합니다. 이 프로젝트는 가상 컨테이너 환경에서 서버 간의 통신이 원활하게 이루어지는 것을 확인하고 이해하는 데 중점을 둡니다.
- 외부에서 원활한 프로젝트 가동을 위해 .gitignore에 포함된 파일들을 예외처리했습니다. 

## 프로젝트 설계도

## 사용 기술 스택
- Docker
- Docker Compose
- Java Spring Boot & JPA
- Thymeleaf
- MySQL
- Nginx

## 프로젝트 실행 방법
- Docker Desktop이 설치되어 있어야 합니다.


- 프로젝트를 Git에서 압축 파일로 다운로드합니다.


- 압축을 풀고 해당 프로젝트 경로에서 터미널을 엽니다.


- 빌드하기
  - 아래 명령어로 이미지를 빌드합니다.
    - DB
      - "docker pull mysql:8.0.40" 명령어로 mysql 이미지를 빌드합니다.
    - WebServer
      - build 폴더에 .jar파일을 도커 컴포즈 build 명령어가 인식하기 때문에 이미지를 빌드하지 않아도 됩니다.
    - Nginx
      - "docker build -t img_nginx:v1 ." 명령어로 빌드합니다.
      
  
  
- 아래 명령어를 실행하여 컨테이너를 빌드하고 실행합니다.
    - docker compose up --build
  

- 터미널에서 웹 서버가 실행된 후 브라우저에서 localhost:80으로 접속합니다.
- 웹 페이지가 정상적으로 출력되면 프로젝트가 성공적으로 실행된 것입니다.

## 프로젝트 구조 및 설명
### 1. 리버스 프록시 서버
- Nginx로 구성된 리버스 프록시 서버입니다.
- myconfig.conf 파일을 생성하여, 80번 포트로 들어온 요청을 **웹 서버(8080 포트)**로 전달하도록 설정했습니다.
  기존의 default.conf가 충돌하여 적용되지 않았기 때문에, Dockerfile에서 해당 파일을 삭제했습니다.

### 2. 웹 서버
- Spring Boot와 JPA를 사용해 구성되었습니다.
- 각 기능을 서비스 레이어로 분리하여 구조화했습니다.
- 가상의 쇼핑몰 환경에서 CRUD 기능을 통해 데이터베이스와의 통신을 확인할 수 있습니다.

### 3. DB 서버
- MySQL로 구성된 데이터베이스 서버입니다.
- init.sql을 작성하여 초기 db 세팅을 입력하여 어느 곳에서든 같은 db 환경을 제공합니다.

### 4. 네트워크
- 각 서버에 대해 네트워크 인터페이스를 생성하여, 필요한 서버들만을 네트워크로 묶어 보안을 강화했습니다.
    ```
    networks:
        frontend_net: # 리버스 프록시 서버가 외부와 통신하는 네트워크
        backend_net_01: # 리버스 프록시 서버와 웹 서버 간의 통신을 위한 네트워크 
        backend_net_02: # 웹 서버와 DB 서버 간의 통신을 위한 네트워크
    ```

<hr />

## 오류 노트 및 최적화 기록
### MySQL 계정 권한 오류
- root 계정으로 접속 시 권한 문제로 인해 데이터베이스와의 통신이 실패하는 경우가 있었습니다.
- MySQL은 root 계정을 기본적으로 외부 접속에 제한하기 때문에, 새 계정을 생성하여 문제를 해결했습니다.
- 해결 방법:
    - sql 수정
       ```sql
       CREATE USER 'newuser'@'%' IDENTIFIED BY 'qwer1234';
       GRANT ALL PRIVILEGES ON *.* TO 'newuser'@'%' WITH GRANT OPTION;
       FLUSH PRIVILEGES;
      ```
    - Spring 설정 파일 수정:
       ```properties 
       spring.datasource.url=jdbc:mysql://mysql-container:3306/shop
       spring.datasource.username=newuser
       spring.datasource.password=qwer1234
      ```

<hr />

### Docker 컨테이너 빌드시 Spring JPA가 DB를 찾지 못하는 오류
- 로컬 개발 환경에서는 localhost를 사용하지만, Docker 환경에서는 컨테이너 이름을 데이터베이스 URL에 입력해야 합니다.
- 해결 방법:
  ```properties
  spring.datasource.url=jdbc:mysql://mysql-container:3306/shop
  ```
  만약 빌드 시 테스트 오류가 발생한다면, 테스트를 생략하고 빌드할 수 있습니다.
   ```
   ./gradlew build -x test
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

### OS영역 파일 시스템 권한 오류 해결

#### 문제 상황 
- Github에 push 후 Docker 명령어 실행 중 아래와 같은 권한 오류가 발생했습니다:
  ```shell
  ERROR [spring-boot-container internal] load build context
  failed to solve: archive/tar: unknown file mode ?rwxr-xr-x
  ```
  
- Github에 push하는 과정에서 작업 PC의 NTFS 파일 시스템에 저장된 파일의 권한 정보가 변경되었습니다.
이로 인해 Windows PowerShell 환경에서는 Docker가 프로젝트 폴더에 접근할 수 없게 되었습니다.


#### 해결 방법
- WSL2 설치 및 사용 
  - Windows Subsystem for Linux 2(WSL2)를 설치하여 Docker 명령어를 WSL2 환경에서 실행했습니다.
  WSL2는 리눅스 환경으로 Docker를 실행하므로, 리눅스 기반의 Docker 작업이 원활히 이루어졌습니다.
  

#### 알게 된 점
- Windows와 Linux 파일 시스템 차이 
  - Windows는 NTFS 파일 시스템을 사용하고, Linux는 ext4 파일 시스템을 사용합니다. 
  - NTFS 파일 시스템은 리눅스 파일 권한 체계와 호환성이 떨어져 리눅스 시스템 실행시 권한 오류가 발생할 수 있습니다. 
  - Github에 파일을 push하거나 pull할 때, 실행 권한(chmod +x)이 유지되지 않아 문제가 발생할 가능성이 있습니다. 
  - Docker Desktop의 구조 
    - Docker Desktop은 Windows에서 실행되지만, 기본적으로 WSL2와 통합됩니다. 
    - WSL2 환경에서 실행되는 Docker는 Linux 기반이므로, NTFS 파일 시스템의 권한 문제를 우회할 수 있습니다. 
    - Docker 명령어를 WSL2에서 실행하면 NTFS 대신 ext4 파일 시스템의 권한 체계를 따르게 됩니다.

  - 권한 오류 해결 
    - WSL2 환경에서 작업하면 NTFS의 권한 제약을 피할 수 있습니다. 
    - 리눅스 파일 시스템에서는 권한 설정을 chmod 명령어로 관리할 수 있습니다.
      ```shell 
      chmod -R 755 ./project-folder
      ```
      

