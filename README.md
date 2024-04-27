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

> Job Instance
1. 기본 개념
   * Job이 실행될 때 생성되는 Job의 논리적 실행 단위 객체로서 고유하게 식별 가능한 작업 실행을 나타냄
   * Job의 설정과 구성은 동일하지만 Job이 실행되는 시점에 처리하는 내용은 다르기 때문에 Job의 실행을 구분해야함
     * 예를 들어 하루에 한 번씩 배치 Job이 실행된다면 매일 생행되는 각각의 Job을 JobInstance로 표현합니다.
   * JobInstance 생성 및 실행
     * 처음 시작하는 Job + Job Parameter 일 경우 새로운 JobInstance 생성
     * 이전과 동일한 Job + Job Parameter로 실행 할 경우 이미 존재하는 JobInstance return
       * 내부적으로 jobName + jobKey(jobParameter 해시 값)를 가지고 JobInstance 객체를 얻음
     * Job과 1:M 관계
2. BATCH_JOB_INSTANCE 테이블과 매핑
   * JOB_NAME(job)과 JOB_KEY(jobParameter 해시 값)가 동일한 데이터는 중복해서 저장할 수 없음

> Job Parameter
1. 기본 개념
   * Job을 실행할 때 함께 포함되어 사용되는 파라미터를 가진 도메인 객체
   * 하나의 Job에 존재할 수 있는 여러개의 JobInstance를 구분하기 위한 용도
   * JobParameters와 JobInstance는 1:1 관계
2. 생성 및 바인딩
   * 애플리케이션 실행 시 주입
     * Java -jar LogBatch.jar requestDate=20210101
   * 코드로 생성
     * JobParameterBuilder, DefaultJobParametersConverter
   * SpEL이용
     * @Value("#{jobParameter[requestDate]}", @JobScope, @StepScope 선언 필수)
3. BATCH_JOB_EXECUTION_PARAM 테이블과 매핑
    * JOB_EXECUTION과 1:M의 관계

> Job Execution
1. 기본 개념
 * JobInstance에 대한 한번의 시도를 의미하는 객체로서 Job 실행 중에 발생한 정보들을 저장하고 있는 객체
   * 시작시간, 종료시간, 상태(시작됨, 왼료, 실패), 종료상태의 속성을 가짐
 * JobInstance와 관계
   * JobExecution은 `FAILED` 또는 `COMPLETED` 등의 Job의 실행 결과 상태를 가지고 있음
   * JobExecution의 실행 상태가 `COMPLETED`면 JobInstance 실행이 완료된 것으로 간주해서 재 실행이 불가함
   * JobExecution의 실행 상태가 `FAILED`면 JobInstance 실행이 완료되지 않은 것으로 간주해서 재실행이 가능함
     * JobParameter가 동일한 값으로 Job을 실행할 지라도 JobInstance를 계속 실행할 수 있음
   * JobExecution의 실행 상태 결과가 `COMPLETED` 될 때까지 하나의 JobInstance 내에서 여러 번의 시도가 생길 수 있음
2. BATCH_JOB_EXECUTION 테이블과 매핑
   * JobInstance와 JobExecution은 1:M의 관계로서 JobInstance에 대한 성공/실패의 내역을 가지고 있음

> Step
1. 기본 개념
   * Batch job을 구성하는 독립적인 하나의 단계로서 실제 배치 처리를 정의하고 컨트롤하는데 필요한 모든 정보를 가지고 있는 도메인 개체
   * 단순한 단일 태스크 뿐 아니라 입력과 처리 그리고 출력과 관련된 복잡한 비즈니스 로직을 포함하는 모든 설정들을 담고 있다.
   * 배치작업을 어떻게 구성하고 실행할 것인지 Job의 세부 작업을 T ask 기반으로 설정하고 명세해 놓은 객체
   * 모든 Job은 하나 이상의 Step으로 구성됨
2. 기본 구현체
   * TaskletStep
     * 가장 기본이 되는 클래스로서 Tasklet 타입의 구현체들을 제어한다.
   * PartitionStep
     * 멀티 스레드 방식으로 Step을 여러 개로 분리해서 실행한다.
   * JobStep
     * Step 내에서 Job을 실행하도록 한다.
   * FlowStep
     * Step 내에서 Flow를 실행하도록 한다.

> Step Execution
1. 기본 개념
   * Step에 대한 한번의 시도를 의미하는 객체로서, Step 실행중에 발생한 정보들을 저장하고 있는 객체
     * 시작시간, 종료시간, 상태(시작됨, 완료됨, 실패), commit count, rollback count 등의 속성을 가짐
   * Step이 매번 시도될 때마다 생성되며 각 Step 별로 생성된다.
   * Job이 재시작 하더라도 이미 성공적으로 완료된 Step은 재실행되지 않고 실패한 Step만 실행된다.
   * 이전 단계 Step이 실패해서 현재 Step을 실행하지 않았다면 Step Executin을 생성하지 않는다. Step이 실제로 시작됐을 때만 StepExecutin을 생성한다.
   * Job Executin과의 관계
     * Step의 StepExecution이 모두 정상적으로 완료되어야 JobExecution이 정상적으로 완료된다.
     * Step의 StepExecution중 하나라도 실패하면 JobExecution은 실패한다.
2. BATCH_STEP_EXECUTION 테이블과 매핑
    * JobExecution과 StepExecution은 1:M의 관계
    * 하나의 Job에 여려 개의 Step으로 구성 했을 경우 각 StepExecution은 하나의 JobExecution을 부모로 가진다

> Step Contribution
1. 기본 개념
    * 청크 프로세스의 변경 사항을 버퍼링 한 후 StepExecution 상태를 업데이트하는 도메인 객체
    * 청크 커밋 직전에 StepExectuion의 apply 메서드를 호출하여 상태를 업데이트함
    * ExitStatus의 기본 종료코드 외 사용자 정의 종료코드를 생성해서 적용 할 수 있음
2. 구조
   * readCount: 성공적으로 read한 아이템 수
   * writeCount: 성공적으로 write한 아이템 수
   * filterCount: itemProcessor에 의해 필터된 아이템 수
   * parentSkipCount: 부코 클래스인 StepExecution의 총 Skip 횟수
   * readSkipCount: read에 실패해서 스킵된 횟수
   * wirteSkipCount: writeSkipCount: write에 실패해서 스킵된 횟수
   * processSkipCount: process에 실패해서 스킵된 횟수
   * ExitStatus: 실행결과를 나타내는 클래스로서 종료코드 포함(UNKNOWN, EXECUTION, COMPLETED, NOOP, FAILED, STOPPED)
   * StepExectuion: StepExectuion 객체 저장

> Execution Context
1. 기본 개념
   * 프레임워크에서 유지 및 관리하는 키/값으로 컬렉션으로 StepExectuion 또는 JobExecution 객체의 상태(State)를 저장하는 공유 객체
   * DB에 직렬화 한 값으로 저장됨 `{"key": value}`
   * 공유 범위
     * Step 범위 - 각 Step의 StepExecution에 저장되며 Step간 서로 공유 안됨
     * Job 범위 - 각 Job의 JobExecution에 저장되며 Job 간 서로 공유 안되며 해당 Job의 Step 간 서로 공유됨
   * Job 재시작시 임 ㅣ처리한 Row 데이터는 건너뛰고 이후로 수행하도록 할 때 상태 정보를 활용한다.
2. 구조
   * Map<String, object> map = new ConcurrentHashMap
   * 유지, 관리에 필요한 키값 설정

> JobRepository
1. 기본 개념
   * 배치 작업 중의 정보를 저장하는 저장소 역할
   * Job이 언제 수행되었고, 언제 끝났으며, 몇 번이 실행되었고 실행에 대한 결과 등의 배치 작업의 수행과 관련된 모든 meta data를 저장함
     * JobLauncher, Job, Step 내부에서 CRUD 기능을 처리함
2. JobRepository 설정
   * BatchConfigurer 인터페이스를 구현하거나 BasicBatchConfigurer를 상속해서 JobRepository 설정을 커스터마이징 할 수 있다
     * JDBC 방식으로 설정 - JobRepositoryFactoryBean
       * 내부적으로 AOP 기술을 통해 트랜잭션을 처리해주고 있음
       * 트랜잭션 isolation의 기본값은 SERIALIZEBLE로 최고수준, 다른 레벨(READ_COMMITED, REPEATABLE_READ)로 지정 가능
       * 메타테이블 Table Prefix를 변경할 수 있음, 기본 값은 "BATCH_"
       
   * In Memory 방식으로 설정 - MapJobRepositoryFactoryBean
     * 성능 등의 이유로 도메인 오브벡트를 굳이 데이터베이스에 저장하고 싶지 않은 경우
     * 보통 Test나 프로토타입의 빠른 개발이 필요할 때 사용

> JobLauncher
1. 기본 개념
    * 배치 Job을 실행시키는 역할을 한다.
    * Job과 Job Parameters를 인자로 받으며 요청된 배치 작업을 수행한 후 최종 client에게 JobExecutino을 반환함
    * 스프링 부트 배치가 구동되면 JobLauncher 빈이 자동 생성 된다.
    * Job 실행
      * JobLauncher.run(Job, JobParameters)
      * 스프링 부트 배치에는 JobLauncherApplicationRunner가 자동으로 JobLauncher를 실행시킨다.
      * 동기적 실행
        * taskExecutor를 SyncTaskExecutor로 설정할 경우 (기본값은 SyncTaskExecution을 반환)
        * JobExecution을 획득하고 배치 처리를 최종 완료한 이후 Client에게 JobExecution을 반환
        * 스케줄러에 의한 배치처리에 적합 함 - 배치처리시간이 길어도 상관없는 경우
      * 비 동기적 실행
        * taskExecutor가 SimpleAsyncTaskExecutor로 설정할 경우
        * JobExecution을 획득한 후 Client에게 바로 JobExecution을 반환하고 배치처리를 완료한다.
        * HTTP 요청에 의한 배치처리에 적합함 - 배치처리 시간이 길 경우 응답이 늦어지지 않도록 함.
2. 구조
    * 동기적 실행
      1. Client -> JobLauncher.run()
      2. JobLauncher -> Job.execute()
      3. Job -> Step / tasklet 실행
      4. Job return ExitStatus -> JobLauncher
      5. JobLauncher return JobExecution -> Client
    * 비 동기적 실행
      1. Client -> JobLauncher.run()
      2. JobLauncher return JobExecution(ExitStatus.UNKNOWN) -> Client
      3. JobLauncher -> Job.execute()
      4. Job -> Step / tasklet 실행
      5. Job return ExitStatus -> JobLauncher
***
## 스프링 배치 실행 - Job
> 배치 초기화 설정
1. JobLauncherApplicationRunner
    * Spring Batch 작업을 시작하는 ApplicationRunner로서 BatchAutoConfiguration에서 생성됨
    * 스프링 부트에서 제공하는 ApplicationRunner의 구현체로 애플리케이션이 정상적으로 구동되자 마자 실행됨
    * 기본적으로 Bean으로 등록된 모든 Job을 실행시킨다.
2. BatchProperties
   * Spring Batch의 환경 설정 클래스
   * Job 이름, 스키마 초기화 설정, 테이블 Prefix 등의 값을 설정할 수 있다.
   * application.properties or application.yml 파일에 설정함
     ```yml
     spring:
        batch:
          job:
            names: ${job.name:NONE}
          initialize-schema: Never
          tablePrefix: SYSTEM
     ```
3. Job 실행 옵션
    * 지정한 Batch Job만 실행하도록 할 수 있음
    * spring.batch.job.names: ${job.name:NONE}
    * 애플리케이션 실행시 Program arguments로 job 이름을 입력한다.
      * --job.name=helloJob
      * --job.name=helloJob,simpleJob (하나 이상의 job을 실행할 경우 쉼표로 구분해서 입력함)
     
> JobBuilderFactory / JobBuilder
1. 스프링 배치는 Job과 Step을 쉽게 생성 및 설정할 수 있도록 util 성격의 빌더 클래스들을 제공함
2. JobBuilderFactory
   * JobBuilder를 생성하는 팩토리 클래스로서 get(String name) 메서드를 제공
   * jobBuilderFactory.get("jobName")
     * "jobName"은 스프링 배치가 Job을 실행시킬 때 참조되는 Job의 이름
3. JobBuilder
   * Job을 구성하는 설정 조건에 따라 두 개의 하위 빌더 클래스를 생성하고 실제 Job 생성을 위임한다.
   * SimpleJobBuilder
     * SimpleJobBuilder
       * SimpleJob을 생성하는 Builder 클래스
       * Job 실행과 관련된 여러 설정 API를 제공한다.
     * FlowJobBuilder
       * FlowJob을 생성하는 Builder 클래스
       * 내부적으로 FlowBuilder를 반환함으로써 Flow 실행과 관련된 여러 설정 API를 제공한다.

> SimpleJob - 개념 및 API
1. 기본 개념
   * SimpleJob은 Step을 실행시키는 Job 구현체로서 SimpleJobBuilder에 의해 생성된다.
   * 여러 단계의 Step으로 구성할 수 있으며 Step을 순차적으로 실행시킨다.
   * 모든 Step의 실행이 성공적으로 완료되어야 Job이 성공적으로 완료된다.
   * 맨 마지막에 실행한 Step의 BatchStatus가 Job의 최종 BatchStatus가 된다.
2. 흐름
   * 성공
      * SimpleJob -> Step1 -> competed 
      * SimpleJob -> Step2 -> competed
      * SimpleJob -> competed
   * 실패
       * SimpleJob -> Step1 -> failed
       * SimpleJob -> failed (step2는 실행되지 않음)
3. API
   * `start(Step)`: 처음 실행할 Step 설정, 최초 한번 설정, 이 메서드를 실행하면 SimpleJobBuilder를 반환
   * `next(Step)`: 다음에 실행할 Step 설정, 횟수는 제한이 없으며 모든 next()의 Step이 종료가 되면 Job이 종료된다.
   * `incremente(JobParametersIncrementer)`: JobParameter의 값을 자동 증가해 주는 JobParameterIncrementer 설정
   * `preventRestarte(true)`: Job의 재시작 가능 여부 설정, 기본 값은 true
   * `validator(JobParameterValidator)`: JobParameter를 실행하기 전에 올바른 구성이 되었는지 검증하는 JobParametersValidator 설정
   * `listen(JobExecutorListener)`: Job 라이프 사이클의 특정 시점에 콜백 제공박도록 JobExecutionListener 설정
   * `build()`: SimpleJob 생성

> validator()
1. 기본 개념
   * Job이 실행에 꼭 필요한 파라미터를 검증하는 용도
   * DefaultJobParametersValidator 구현체를 지원하며, 좀 더 복잡한 제약 조건이 있다면 인터페이스를 구현할 수도 있음
2. 구조
   * JobParameter 값을 매개변수로 받아 검증함

> preventRestart()
1. 기본 개념
   * Job의 재시작 여부를 설정
   * 기본 값은 true이며 false로 설정시 '이 Job은 재시작을 지원하지 않는다.'라는 의미
   * Job이 실패해도 재시작이 안되며 Job을 재시작하려고 하면 JobRestartException이 발생
   * 재시작과 관련 있는 기능으로 Job을 처음 실행하는 것과는 아무런 상관 없음
2. 흐름도
   * Job -> exsit Job Execution ?
   * yes -> preventRestart? 
     * false -> JobRestartException 
     * true  -> JobInstance -> JobExecution -> Business 
   * no  -> JobInstance -> JobExecution -> Business

> incrementer()
1. 기본 개념
   * JobParameters에서 필요한 값을 증가시켜 다음에 사용될 JobParameters 오브젝트를 리턴
   * 기존의 JobParameter 변경없이 Job을 여러 번 시작하고자 할 때
   * RunIdIncrementer 구현체를 지원하며 인터페이스를 직접 구현할 수 있음
2. 구조
   * JobParametersIncrementer
     * JobParameters getNext(@Nullalbe JobParameters parameters);

***
## 스프링 배치 실행 - StepBuilderFactory/StepBuilder
> StepBuilderFactory
1. StepBuilderFactory
   * StepBuilder를 생성하는 팩토리 클래스로서 get(String name) 메서드 제공
   * StepBuilderFactory.get("stepName")
     * "stepName"으로 Step을 생성
2. StepBuilder
   * Step을 구성하는 설정 조건에 따라 다섯 개의 하위 빌더 클래스를 생성하고 실제 Step 생성을 위임한다.
   * TaskletStepBuilder
     * TaskletStep을 생성하는 기본 빌더 클래스
   * SimpleStepBuilder
     * TaskletStep을 생성하며 내부적으로 청크기반의 작업을 처리하는 ChunckOrientedTasklet 클래스를 생성한다.
   * PartitionStepBuilder
     * PartitionStep을 생성하며 멀티 스레드 방식으로 Job을 실행한다.
   * JobStepBuilder
     * JobStep을 생성하여 Step 안에서 Job을 실행한다.
   * FlowStepBuilder
     * FlowStep을 생성하여 Step 안에서 Flow를 실행한다.

> TaskletStep 개념 및 API
1. 기본 개념
   * 스프링 배치에서 제공하는 Step의 구현체로서 Tasklet을 실행시키는 도메인 객체
   * RepeatTemplate를 사용해서 Tasklet의 구문을 트랜잭션 경계 내에서 반복해서 실행함
   * Task 기반과 Chunk 기반으로 나누어서 Tasklet을 실행함
2. Task VS Chunk 기반 비교
   * 스프링 배치에서 Step의 실행 단위는 크게 2가지로 나누어짐
     * Tasklet 기반
       * ItemReader와 ItemWriter와 같은 chunk 기반의 작업 보다 단일 작업 기반으로 처리되는 것이 더 효율적인 경우
       * 주로 Tasklet 구현체를 만들어 사용
       * 대량 처리를 하는 경우 chunk 기반에 비해 더 복잡한 구현 필요
     * Chunk 기반
       * 하나의 큰 덩어리를 n개씩 나눠서 실행한다는 의미로 대량 처리를 하는 경우 효과적으로 설계됨
       * ItemReader, ItemProcessor, ItemWriter를 사용하며 청크 기반 전용 Tasklet인 ChunkOrientedTasklet 구현체가 제공된다.
3. 구조
   * Task 기반
     * Job -> TaskletStep -> RepeatTemplate -> Transaction 내에서 [Tasklet -> Business Logic]
   * Chunk 기반
     * Job -> TaskletStep -> RepeatTemplate -> Transaction 내에서 [ChunkOrientedTasklet -> ItemReader, ItemProcessor, ItemWriter]

```java
public Step batchStep() {
    return stepBuilderFactory.get("batchStep")  // StepBuilder를 생성하는 팩토리, Step의 이름을 매개변수로 받음
            .tasklet(Tasklet)                   // Tasklet 클래스 설정, 이 메서드를 실행하면 TaskletStepBuilder 반환
            .startLimit(10)                     // Step의 실행 횟수를 설정. 설정한 만큼 실행되고 초과시 오류 발생. 기본값은 INTEGER.MAX_VALUE
            .allowStartIfComplete(true)         // Step의 성공, 실패와 상관없이 항상 Step을 실행하기 위한 설정
            .listenr(StepExecutionListener)     // Step 라이프 사이클의 특정 시점에 콜백 제공 받도록 StepExecutionListener 설정
            .build();                           // TaskletStep을 생성
}
```
> TaskletStep - tasklet()
1. 기본 개념
   * Tasklet 타입의 클래스를 설정한다.
     * Tasklet
       * Step 내에서 구성되고 실행되는 도메인 객체로서 주로 단일 태스크를 수행하기 위한 것
       * TaskletStep에 의해 반복적으로 수행되며 반환값에 따라 계속 수행 혹은 종료한다.
       * RepeatStatus - Tasklet의 반복 여부 상태 값
         * RepeatStatus.FINISHED -> Tasklet 종료 RepeatStatus를 null 반환하며 RepeatStatus.FINISHED로 해석됨
         * RepeatStatus.CONTINUABLE -> Tasklet 반복
         * RepeatStatus.FINISHED가 리턴되거나 실패 예외가 던져지기 전까지 TaskletStep에 의해 while문 안에서 반복적으로 호출됨(무한 루프 주의)
   * 익명 클래스 혹은 구현 클래스를 만들어 사용한다.
   * 이 메소드를 실행하면 TaskletStepBuilder가 반환되어 관련 API를 설정할 수 있다.
   * Step에 오직 하나의 Tasklet 설정이 가능하며 두개 이상을 설정 했을 경우 마짐가에 설정한 객체가 실행된다.
2. 구조
```java
public interface Tasklet {
    @Nullable
    RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception;
}

```

> TaskletStep - startLimit()
1. 기본 개념
   * Step의 실행 횟수를 조정할 수 있다
   * Step 마다 설정할 수 있다
   * 설정 값을 초과해서 다시 실행하려고 하면 StartLimitExceededException이 발생
   * start-lmit의 디폴트 값은 Integer.MAX_VALUE

> TaskletStep - allowStartIfComplete()
1. 기본 개념
   * 재시작 가능한 job에서 Step의 이전 성공 여부와 상관없이 항상 step을 실행하기 위한 설정
   * 실행 마다 유효성을 검증하는 Step이나 사전 작업이 꼭 필요한 Step 등
   * 기본적으로 COMPLETED 상태를 가진 Step이나 사전 작업이 꼭 필요한 Step 등
   * allow-start-if-complete가 true로 설정된 step은 항상 실행한다.

> JobStep
1. 기본 개념
    * Job이 속하는 Step 중 외부의 Job을 포함하고 있는 Step
    * 외부의 Job이 실패하면 해당 Step이 실패하므로 결국 최종 기본 Job도 실패한다.
    * 모든 메타데이터는 기본 Job과 외부 Job 별로 각각 저장된다.
    * 커다란 시스템을 작은 모듈로 쪼개고 job의 흐름을 관리하고자 할 때 사용할 수 있다.
2. API 소개
```java
public Step jobStep() {
    return StepBuilderFactory.get("jobStep")            // StepBuilder를 생성하는 팩토리, Step의 이름을 매개변수로 받음
            .job(job)                                   // JobStep 내에서 실행될 Job 설정 JobStepBuilder 반환
            .launcher(JobLauncher)                      // Job을 실행할 JobLauncher 설정
            .parametersExtractor(JobParametersExtractor)// Step의 ExecutionContext를 Job이 실행되는 데 필요한 JobParameters로 변환
            .build();                                   // JobStep을 생성
}                           
```

> FlowJob 개념 및 API
1. 기본 개념
    * Step을 순차적으로만 구성하는 것이 아닌 특정한 상태에 따라 흐름을 전환하도록 구성할 수 있으며 FlowJobBuilder에 의해 생성된다.
      * Step이 실패하더라도 Job은 실패로 끝나지 않도록 해야하는 경우
      * Step이 성공했을 때 다음에 실행해야 할 Step을 구분해서 실행 해야 하는 경우
      * 특정 Step은 전혀 실행되지 않게 구성 해야 하는 경우
    * Flow와 Job의 흐름을 구성하는데만 관여하고 실제 비즈니스 로직은 Step에서 이루어진다.
    * 내부적으로 SimpleFlow 객체를 포함하고 있으며 Job 실행시 호출한다.
2. SimpleJob VS FlowJob
    * SimpleJob - 순차적 흐름
      * Step A가 가장 먼저 실행
      * Step A가 실패하면 전체 Job이 실패
      * Step B가 Step C는 실행되지 않음
    * FlowJob - 조건적 흐름
      * Step A가 가장 먼저 실행
      * Step A가 성공하면 Flow가 실행
      * Step A가 실패하면 Step B가 실행
      * Step A의 성공/실패에 관계없이 Job이 성공한다.
3. API
```java
public Job batchJob() {
    return jobBuilderFactory.get("batchJob")
            .start(Step)                                    // Flow 시작하는 Step 설정
            .on(String pattern)                             // Step의 실행 결과로 돌려 받는 종료 상태(ExitStatus)를 캐치하여 매칭하는 패턴, TransitionBuilder 반환
            .to(Step)                                       // 다음 이동할 Step 지정
            .stop() / fail() / end() / stopAndRestart()     // Flow를 중지 / 실패 / 종료 하도록 Flow 종료
            .form(Step)                                     // 이전 단계에서 정의한 Step의 Flow를 추가적으로 정의함
            .next(Step)                                     // 다음으로 이동할 Step 지정
            .end()                                          // build() 앞에 위차하면 FlowBuilder를 종료하고 SimpleFlow 객체 생성
            .build();                                       // FlowJob 생성하고 flow 필드에 SimpleFlow 저장
}
```

> FlowJob - start() / next()
```java
public Job batchJob() {
    return jobBuilderFactory.get("batchJob")
            .start(Step)                        // 처음 실행할 Flow설정, JobFlowBuilder가 반환된다.
                                                // 여기에 Step이 인자로 오게 되면 SimpleJobBuiler가 반환된다.
            .on(String pattern)
            .to(Step)
            .stop() / fail() / end() / stopAndRestart()
            .form(Step)
            .next(Step)
            .end()
            .build();
}
```
