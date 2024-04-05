# Spring Batch
***
## 스프링 배치 시작
> 스프링 배치 활성화
  * `@EnableBatchProcessing` 스프링 배치가 작동하기 위해 선언해야 하는 애노테이션
    * 총 4개의 설정 클래스를 실행시키며 스프링 배치의 모든 초기화 및 실행 구성이 이루어진다.
    * 스프링 부트 배치의 자동 설정 클래스가 실행됨으로 빈으로 등록된 모든 Job을 검색해서 초기화와 동시에 Job을 수행하도록 구성됨
> 스프링 배치 초기화 설정 클래스
1. BatchAutoConfiguration
    * 스프링 배치가 초기화 될 때 자동으로 실행되는 설정 클래스
    * Job을 수행하는 JobLauncherApplicationRunner 빈을 생성
2. SimpleBatchConfiguration
    * JobBuilderFactory와 StepBuilderFactory 생성
    * 스프링 배치의 주요 구성 요소 생성 - 프록시 객체로 생성됨
3. BatchConfigurerConfiguration
    * BasicBatchConfigurer
      * SimpleBatchConfiguration에서 생성한 프록시 객체의 실제 대상 객체를 생성하는 설정 클래스
      * 빈으로 의존성 주입 받아서 주요 객체들을 참조해서 사용할 수 있다.
    * JpaBatchConfigurer
      * JPA 관련 객체를 생성하는 설정 클래스
    * 사용자 정의 BatchConfigurer 인터페이스를 구현하여 사용할 수 있음
> Hello Spring Batch 시작

```java
@Configuration // 하나의 배치 Job을 정의하고 빈 설정
public class HelloJobConfiguration { // Job을 정의
   private final JobBuilderFactory jobBuilderFactory;      // Job을 생성하는 빌더 팩토리
   private final StepBuilderFactory stepBuilderFactory;    // Step을 생성하는 빌더 팩토리

   public HelloJobConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
      this.jobBuilderFactory = jobBuilderFactory;
      this.stepBuilderFactory = stepBuilderFactory;
   }

   @Bean
   public Job helloJob() {
       return jobBuilderFactory.get("helloJob") // Job 생성
               .start(helloStep())
               .build();
   }
   
   @Bean
   public Step helloStep() {
       return stepBuilderFactory.get("helloStep")   // Step 생성
               .tasklet((contribution, chunkContext) -> {
                  System.out.println("Hello Spring Batch");
                  return RepeatStatus.FINISHED;
               })
               .build();
   }
}
```
* Job 구동 -> Step을 실행 -> Tasklet을 실행
> DB 스키마 생성 및 이해
1. 스프링 배치 메타 데이터
   * 스프링 배치의 실행 및 관리를 위한 목적으로 여러 도메인들(Job, Step, JobParameters)의 정보들을 저장, 업데이트, 조회할 수 있는 스키마 제공
   * 과거, 현재의 실행에 대한 세세한 정보, 실행에 대한 성공과 실패 여부 등을 일목요연하게 관리함으로서 배치운용에 있어 리스크 발생시 빠른 대처 가능
   * DB와 연동할 경우 필수적으로 메타 테이블이 생성 되어야 함
2. DB 스키마 제공
   * 파일 위치: `/org/springframework/batch/core/schema-*.sql`
   * DB 유형별로 제공
3. 스키마 생성 설정
   * 수동 생성 - 쿼리 복사 후 직접 실행
   * 자동 생성 - spring.batch.jdbc.initialize-schema 설정
     * ALWAYS
       * 스크립트 항상 실행
       * RDBMS 설정이 되어 있을 경우 내장 DB 보다 우선적으로 실행
     * EMBEDDED : 내장 DB일 때만 실행되며 스키마가 자동 생성됨, 기본 값
     * NEVER
       * 스크립트 항상 실행 안함
       * 내장 DB 일 경우 스클비트가 생성이 안되기 때문에 오류 발생
       * 운영에서 수동으로 스크립트 생성 후 설정하는 것을 권장
> Job 관련 테이블
* BATCH_JOB_INSTANCE
  * Job이 실행될 때 JobInstance 정보가 저장되며 `job_name`과 `job_key`를 키로 하여 하나의 데이터가 저장
  * 동일한 `job_name`과 `job_key`로 중복 저장 될 수 없다.

| 컬럼              | 내용                                   |
|-----------------|--------------------------------------|
| JOB_INSTANCE_ID | 고유하게 식별할 수 있는 기본 키                   |
| VERSION         | 업데이트 될 때마다 1씩 증가                     |
| JOB_NAME        | Job을 구성할 때 부여하는 Job의 이름              |
| JOB_KEY         | job_name과 jobParameter를 합쳐 해싱한 값을 저장 |

* BATCH_JOB_EXECUTION
  * Job의 실행정보가 저장되며 Job 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리

| 컬럼               | 내용                                                                            |
|------------------|-------------------------------------------------------------------------------|
| JOB_EXECUTION_ID | 고유하게 식별할 수 있는 기본 키, JOB_INSTANCE와 일대다 관계                                     |
| VERSION          | 업데이트 될 때마다 1씩 증가                                                              |
| JOB_INSTANCE_ID  | JOB_INSTANCE의 키 저장                                                            |
| CREATE_TIME      | 실행(Execution)이 생성된 시점을 TimeStamp 형식으로 기록                                      |
| START_TIME       | 실행(Execution)이 시작된 시점을 TimeStamp 형식으로 기록                                      |
| END_TIME         | 실행이 종료된 시점을 TimeStamp 형식으로 기록하며 Job 실행도중 오류가 발생해서 Job이 중단된 경우 값이 저장되지 않을 수 있음 |
| STATUS           | 실행상태(BatchStatus)를 저장 (COMPLETE, FAILED, STOPPED...)                          |
| EXIT_CODE        | 실행 종료코드(ExitStatus)를 저장 (COMPLETED, FAILED...)                                |
| LAST_UPDATED     | 마지막 실행(Exectuion) 시점을 TimeStamp 형식으로 기록                                       |

* BATCH_JOB_EXECUTION_PARAMS
  * Job과 함께 실행되는 JobParameter 정보를 저장

| 컬럼               | 내용                                       |
|------------------|------------------------------------------|
| JOB_EXECUTION_ID | JobExecution 식별키, JOB_EXECUTION과는 일대다 관계 |
| TYPE_CD          | STRING, LONG, DATE, DUBLE 타입 정보          |
| KEY_NAME         | 파라미터 키 값                                 |
| STRING_VAL       | 파라미터 문자 값                                |
| DATE_VAL         | 파라미터 날짜 값                                |
| LONG_VAL         | 파라미터 LONG 값                              |
| DOUBLE_VAL       | 파라미터 DOUBLE 값                            |
| IDENTIFYING      | 식별여부 (TRUE, FALSE)                       |

* BATCH_JOB_EXECUTION_CONTEXT
  * Job이 실행되는 동안 여러가지 상태정보, 공유 데이터를 직렬화(JSON 형식) 해서 저장
  * Step 간 서로 공유 가능함

| 컬럼                | 내용                                      |
|--------------------|-----------------------------------------|
| JOB_EXECUTION_ID   | JobExecution 식별키, JOB_EXECUTION 마다 각각 생성 |
| SHORT_CONTEXT      | JOB의 실행 상태 정보, 공유 데이터 등의 정보를 문자열로 저장  |
| SERIALIZED_CONTEXT | 직렬화(serialized)된 전체 컨텍스트                |

> Step 관련 테이블
* BATCH_STEP_EXECUTION
  * Step의 실행정보가 저장되며 생성, 시작, 종료 시간, 실행상태, 메시지 등을 관리

| 컬럼                | 내용                                                                            |
|--------------------|-------------------------------------------------------------------------------|
| STEP_EXECUTION_ID  | Step의 실행정보를 고유하게 식별할 수 있는 기본 키                                                |
| VERSION            | 업데이트 될 때마다 1씩 증가                                                              |
| STEP_NAME          | Step을 구성할 때 부여하는 Step의 이름                                                     |
| JOB_EXECUTION_ID   | JobExecution 기본키, JobExecution과는 일대다 관계                                       |
| START_TIME         | 실행(Execution)이 시작된 시점을 TimeStamp 형식으로 기록                                      |
| END_TIME           | 실행이 종료된 시점을 TimeStamp 형식으로 기록하며 Job 실행도중 오류가 발생해서 Job이 중단된 경우 값이 저장되지 않을 수 있음 |
| STATUS             | 실행 상태 (BatchStatus)를 저장 (COMPLETED, FAILED, STOPPED...)                       |
| COMMIT_COUNT       | 트랜잭션 당 커밋되는 수를 기록                                                             |
| READ_COUNT         | 실행시점에 READ한 ITEM 수를 기록                                                        |
| FILTER_COUNT       | 실행도중 필터링된 ITEM 수를 기록                                                          |
| WRITE_COUNT        | 실행도중 저장되고 커밋된 ITEM 수를 기록                                                      |
| READ_SKIP_COUNT    | 실행도중 READ가 SKIP된 ITEM 수를 기록                                                   |
| WRITE_SKIP_COUNT   | 실행도중 WRITE가 SKIP된 ITEM 수를 기록                                                  |
| PROCESS_SKIP_COUNT | 실행도중 PROCESS가 SKIP된 ITEM 수를 기록                                                |
| ROLLBACK_COUNT     | 실행도중 ROLLBACK이 일어난 수를 기록                                                      |
| EXIT_CODE          | 실행종료 코드(ExitStatus)를 저장 (COMPLETED, FAILED...)                                |
| EXIT_MESSAGE       | Status가 실패일 경우 실패 원인 등의 내용을 저장                                                |
| LAST_UPDATED       | 마지막 실행(Execution) 시점을 TimeStamp 형식으로 기록                                       |

* BATCH_STEP_EXECUTION_CONTEXT
  * Step이 실행되는 동안 여러가지 상태 정보, 공유 데이터를 직렬화(JSON 형식) 해서 저장
  * Step 별로 저장되며 Step 간 서로 공유할 수 없음

| 컬럼                | 내용                                  |
|--------------------|-------------------------------------|
| STEP_EXECUTION_ID  | Step의 실행정보를 고유하게 식별할 수 있는 기본 키      |
| SHORT_CONTEXT      | STEP의 실행 상태정보, 공유데이터 등의 정보를 문자열로 저장 |
| SERIALIZED_CONTEXT | 직렬화(serialized)된 전체 컨텍스트                 |
***
## 스프링 배치 도메인 이해
> Job
1. 기본 개념
   * 배치 계층 구조에서 가장 상위에 있는 개념으로서 하나의 배치 작업 자체를 의미함
     * 예) `API 서버의 접속 로그 데이터를 통계 서버로 옮기는 배치`인 Job 자체를 의미한다.
   * Job Configuration을 통해 생성되는 객체 단위로서 배치작업을 어떻게 구성하고 실행할 것인지 전체적으로 설정하고 명세해 놓은 객체
   * 배치 Job을 구성하기 위한 최상위 인터페이스이며 스프링 배치가 기본 구현체를 제공한다.
   * 여러 Step을 포함하고 있는 컨테이너로서 반드시 한개 이상의 Step으로 구성해야함.
2. 기본 구현체
   * SimpleJob
     * 순차적으로 Step을 실행시키는 Job
     * 모든 Job에서 유용하게 사용할 수 있는 표준 기능을 갖고 있음
   * FlowJob
     * 특정한 조건과 흐름에 따라 Step을 구성하여 실행시키는 Job
     * Flow 객체를 실행시켜서 작업을 진행함