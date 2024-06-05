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
> Transition - 배치상태 유형(BatchStatus / ExitStatus / FlowExecutionStatus)
* BatchStatus
  * JobExecution과 StepExecution의 속성으로 Job과 Step의 종료 후 최종 결과 상태가 무엇인지 정의
  * SimpleJob
    * 마지막 Step의 BatchStatus 값을 Job의 최종 BatchStatus 값으로 반영
    * Step이 실패할 경우 해당 Step이 마지막 Step이 된다.
  * FlowJob
    * Flow내 Step의 ExitStatus 값을 FlowExecutionStatus 값으로 저장
    * 마짐가 Flow의 FlowExecutionStatus 값을 Job의 최종 BatchStatus 값으로 반영
* COMPLETE, STARTING, STARTED, STOPPING, STOPPED, FAILED, ABANDONED, UNKNOWN
* ABANDONED는 처리를 완료했지만 성공하지 못한 단계와 재시작시 건너 뛰어야하는 단계
* ExitStatus
  * JobExectuion과 StepExecution의 속성으로 Job과 Step의 실행 후 어떤 상태로 종료되었는지 정의
  * 기본적으로 ExitStatus는 BatchStatus와 동일한 값으로 설정된다.
  * SimpleJob
    * 마지막 Step의 ExitStatus 값을 Job의 최종 ExitStatus 값으로 반영
  * FlowJob
    * Flow내 Step의 ExitStatus 값을 Job의 최종 ExitStatus 값으로 반영
    * 마지막 Flow의 FlowExecutionStatus 값을 Job의 최종 ExitStatus 값으로 반영
  * UNKNOWN, EXECUTING, COMPLETED, NOOP, FAILED, STOPPED

> Transition - on() / to() / stop(), fail(), end(), stopAndRestart()
1. 기본 개념
   * Transition
     * Flow 내 step의 조건부 전환(전이)을 정의함
     * Job의 API 설정에서 on(String pattern) 메소드를 호출하면 TransitionBuilder가 반환되어 Transition Flow를 구성할 수 있음
     * Step의 종료상태(StepExitStatus)가 어떤 pattern과도 매칭되지 않으면 스프링 배치에서 예외를 발생하고 Job은 실패
     * transition은 구체적인 것부터 그렇지 않은 순서로 적용된다.
2. API
   * on(String pattern)
     * Step의 실행 결과로 돌려받은 종료상태(ExitStatus)와 매칭하는 패턴 스키마, BatchStatus와 매칭하는 것이 아님
     * pattern과 ExitStatus와 매칭이 되면 다음으로 실행할 Step을 지정할 수 있다.
     * 특수문자는 두 가지만 허용
       * "*": 0개 이상의 문자와 매칭, 모든 ExitStatus와 매칭된다.
       * "?": 정확히 1개의 문자와 매칭
         * ex) `c*t`는 `cat`과 `count`에 매칭되고, `c?t`는 `cat`에만 매칭된다.
   * to()
     * 다음으로 실행할 단계를 지정
   * from()
     * 이전 단계에서 정의한 Transition을 새롭게 추가 정의함
3. Job을 중단하거나 종료하는 Transition API
  * Flow가 실행되면 FlowExecutionStatus에 상태값이 저장되고 최종적으로 Job의 BatchStatus와 ExitStatus에 반영돤다.
  * Step의 BatchStatus 및 ExitStatus에는 아무런 영향을 주지 않고 Job의 상태만 변경한다.
  * Stop()
    * FlowExecutionStatus가 `STOPPED` 상태로 종료되는 transition
    * Job의 BatchStatus와 ExitStatus가 `STOPPED`으로 종료됨
  * fail()
    * FlowExecutionStatus가 `FAILED` 상태로 죵료되는 transition
    * Job의 BatchStatus와 ExitStatus가 `FAILED`으로 종료됨
  * end()
    * FlowExecutionStatus가 `COMPLETED` 상태로 종료되는 transition
    * Job의 BatchStatus와 ExitStatus가 `COMPLETED`으로 종료됨
    * Step의 ExitStatus가 `FAILED`이더라도 Job의 BatchStatus가 `COMPLETED`로 종료하도록 가능하며 이 때 Job은 재시작은 불가능함
  * stopAndRestart()
    * stop() transition과 기본 흐름은 동일
    * 특정 step에서 작업을 중단하도록 설정하면 중단 이전의 Step만 `COMPLETED` 저장되고 이후의 step은 실행되지 않고 `STOPPED` 상태로 Job 종료
    * Job이 다시 실행됐을 때 실행해야 할 step을 restart인자로 넘기면 이전에 `COMPLETED`로 저장된 step은 건너뛰고 중단 이후 step부터 시작한다.

> 사용자 정의 ExitStatus
1. 기본 개념
   * ExitStatus에 존재하지 않은 exitCode를 새롭게 정의해서 설정
   * StepExecutionListener의 afterStep() 메서드에서 Custom exitCode 생성후 새로운 ExitStatus 반환
   * Step 실행 후 완료 시점에서 현재 exitCode를 사용자 정의 exitCode로 수정할 수 있음

```java
static class PassCheckingListener extends StepExcutionListenerSupport {
    public ExitStatus afterStep(StepExcution stepExcution) {
        String exitCode = stepExcution.getExitStatus().getExitCode();
        if (!exitCode.equals(ExitStatus.FAILED.getExitCode())) {
            return new ExitStatus("DO PASS");
        } else {
            return null;
        }
    }
}
```
> JobExecutionDecider
1. 기본 개념
    * ExitStatus를 조작하거나 StepExecutionListener를 등록할 필요 없이 Transition 처리를 위한 전용 클래스
    * Step과 Transition 역할을 명확히 분리해서 설정 할 수 있음
    * Step과 ExitStatus가 아닌 JobExecutionDecider의 FlowExecutionStatus 상태값을 새롭게 설정해서 반환함

> SimpleFlow 개념 및 API
1. 기본 개념
   * 스프링 배치에서 제공하는 Flow의 구현체로서 각 요소 (Step, Flow, JobExecutionDecider)들을 담고 있는 State를 실행시키는 도메인 객체
   * FlowBuilder를 사용해서 생성하며 Transition과 조합하여 여러 개의 Flow 및 중첩 Flow를 만들어 Job을 구성할 수 있다.
2. 구조

```yml
Flow
  getName()   # Flow 이름 조회
  State getState(String stateName)  # State명으로 State 반환
  FlowExecution start(FlowExecution executor) # Flow를 실행시키는 Start 메소드, FlowExecutor를 넘겨주어 실행을 위임함, 실행후 FlowExecution을 반환
  FlowExecution resume(String stateName, FlowExecutor executor) # 다음에 실행할 State를 구해서 FlowExecutor에게 실행을 위임함
  Collection<State> getState()  # Flow가 가지고 있는 모든 State를 Collection 타입을 반환
```
```yml
SimpleFlow
  String name # Flow 이름
  State startState # State들 중에서 처음 실행할 State
  Map<String, Set<StateTransition>> transitionMap # State명으로 매핑되어있는 Set<StateTransition>
  List<StateTransition> stateTransitions  # State명으로 매핑되어있는 State 객체
  Comparator<StateTransition> stateTransitionComparator # State와 Transition 정보를 가지고 있는 StateTransition 리스트
```
```java
public Job batchJob() {
    return new JobBuilder("flowJob", jobRepository)
            .start(flow1())                 // Flow를 정의하여 설정함
            .on("COMPLETE").to(flow2())     // Flow를 Transition과 함께 구성
            .end()                          // SimpleFlow객체 생성
            .build();                       // FlowJob객체 생성
}
```
> FlowStep
1. 기본 개념
   * Step 내에 Flow를 할당하여 실행시키는 도메인 객체
   * flowStep의 BatchStatus와 ExitStatus은 Flow의 최종 상태값에 따라 결정 된다.
2. API
```java
public Step flowStep() {
    return stepBuilderFactory.get("flowStep")
            .flow(flow()) // Step 내에서 실행될 flow 설정, FlowStepBuilder 반환
            .build();     // FlowStep 객체 생성
}
```

> @JobScope / @StepScope
1. 기본 개념
   * Scope
     * 스프링 컨테이너에서 빈이 관리되는 범위
     * singleton, prototype, request, session, application 있으며 기본은 singleton으로 생성됨
   * 스프링 배치 스코프
     * `@JobScope`, `@StepScope`
       * Job과 Step의 빈 생성과 실행에 관여하는 스코프
       * 프록시 모드를 기본값으로하는 스코프 - `@Scope(value = "job", proxyMode = ScopedProxyMode.TARGET_CLASS`)
       * 해당 스코프가 선언되며 빈이 생성이 애플리케이션 구동시점이 아닌 빈 실행시점에 이루어진다.
         * @Values를 주입해서 빈의 실행 시점에 값을 참조할 수 있으며 일종의 Lazy Binding이 가능해진다.
         * `@Value("#(jobParameters[파라미터명])")`, `@Value("#(jobExecutionContext[파라미터명])")`, `@Value("#(stepExecutionContext[파라미터명])")`
         * @Values를 사용할 경우 빈 선언문에 @JobScope, @StepScope를 정의하지 않으면 오류를 발생하므로 반드시 선언해야 한다.
       * 프록시 모드로 빈이 선언되었기 때문에 애플리케이션 구동시점에는 빈의 프록시 객체가 생성되어 실행 시점에 실제 빈을 호출해 준다.
       * 병렬처리 시 각 스레드 마다 생성된 스코프 빈이 할당되기 때문에 스레드에 안전하게 실행이 가능하다.
   * @JobScope
     * Step 선언문에 정의한다.
     * @Value: jobParameter, jobExecutinContext만 사용가능
   * @StepScope
     * Tasklet이나 ItemReader, ItemWriter, ItemProcessor 선언문에 정의한다.
     * @Value: jobParameter, jobExecutionContext, stepExecutionContext 사용가능

2. 아키텍처
    1. Proxy 객체 생성
       * `@JobScope`, `@StepScope` 어노테이션이 붙은 빈 선언은 내부적으로 빈의 Proxy 객체가 생성된다.
         * `@JobScope`
           * `@Scope(value = "job", proxyMode = ScopedProxyMod.TARGET_CLASS)`
         * `@StepScope`
           * `@Scope(value = "step", proxyMode = ScopedProxyMode.TARGET_CLASS)`
       * Job 실행 시 Proxy 객체가 실제 빈을 호출해서 해당 메서드를 실행시키는 구조
    2. JobScope, StepScope
       * Proxy 객체의 실제 대상이 되는 Bean을 등록 해제하는 역할
       * 실제 빈을 저장하고 있는 JobContext, StepContext를 가지고 있다.
    3. JobContext, StepContext
       * 스프링 컨테이너에서 생성된 빈을 저장하는 컨텍스트 역할
       * Job의 실행 시점에 프록시 객체가 실제 빈을 참조할 때 사용됨

## 스프링 배치 청크 프로세스 이해
> Chunk
1. 기본 개념
    * Chunk란 여러 개의 아이템을 묶은 하나의 덩어리, 블록을 의미한다.
    * 한번에 하나씩 아이템을 입력 받아 Chunk 단위의 덩어리로 만든 후 Chunk 단위로 트랜잭션을 처리한다. 즉 Chunk 단위의 Commit과 Rollback이 이루어진다.
    * 일반적으로 대용량 데이터를 한번에 처리하는 것이 아닌 Chunk 단위로 쪼개어서 더 이상 처리할 데이터가 없을 때까지 반복해서 입출력하는데 사용된다.
    * Chunk<I> vs Chunk<O>
      * Chunk<I>는 ItemReader로 읽은 하나의 아이템을 Chunk에서 정한 개수만큼 반복해서 저장하는 타입
      * Chunk<O>는 ItemReader로부터 전달받은 Chunk<I>를 참조해서 ItemProcessor에 적절하게 가공, 필터링한 다음 ItemWriter에 전달하는 타입

> ChunkOrientedTasklet 개념 및 API
1. 기본 개념
    * ChunkOrientedTasklet은 스프링 배치에서 제공하는 Tasklet의 구현체로서 Chunk 지향 프로세싱을 담당하는 도메인 객체
    * ItemReader, ItemWriter, ItemProcessor를 사용해 Chunk 기반의 데이터 입축력 처리를 담당한다.
    * TaskletStep에 의해서 반복적으로 실행되며 ChunkOrientedTasklet 이 실행 될 때마다 매번 새로운 트랜잭션이 생성되어 처리가 이루어진다.
    * execption이 발생할 경우, 해당 Chunk는 롤백 되며 이전에 커밋한 Chunk는 완료된 상태가 유지된다.
    * 내부적으로 ItemReader를 핸들링하는 ChunkProvider와 ItemProcessor, ItemWirter를 핸들링하는 ChunkProcessor 타입의 구현체를 가진다.

2. API
```java
public Step chunkStep() {
    return stepBuilderFactory.get("chunkStep")
            .<I, O> chunk(10)                   // chunk size 설정, chunkSize는 commit interval을 의미함, input, output 제네릭타입 설정
            .<I, O> chunk(CompletionPolicy)     // chunk 프로세스를 완료하기 위한 정책 설정 클래스 지정
            .reader(itemReader())               // 소스로부터 item을 읽거나 가져오는 itemReader 구현체 설정
            .wirter(itemWriter())               // item을 목적지에 쓰거나 보내기 위한 itemWriter 구현체 설정
            .processor(itemProcessor())         // item을 변형, 가공, 필터링 하기 위한 itemProcessor 구현체 설정
            .stream(ItemStream())               // 재시작 데이터를 관리하는 콜백에 대한 스트림 등록
            .readerIsTransactionQueue()         // Item이 JMS, MessageQueueServer와 같은 트랜잭션 외부에서 읽혀지고 캐시할 것인지 여부, 기본 값은 false
            .listener(ChunkListener)            // chunk 프로세스가 진행되는 특정 시점에 콜백 제공받도록 chunkListener 설정
            .build();
}
```
> ChunkProvider    
1. 기본 개념
   * ItemReader를 사용해서 소스로부터 아이템을 Chunk size 만큼 읽어서 Chunk 단위로 만들어서 제공하는 도메인 객체
   * Chunk<I>를 만들고 내부적으로 반복문을 사용해서 ItemReader.read()를 계속 호출하면서 item을 Chunk에 쌓는다.
   * 외부로부터 ChunkProvider가 호출 될 때마다 항상 새로운 Chunk가 생성된다.
   * 반복문 종료 시점
     * Chunk size 만큼 item을 읽으면 반복문 종료되고 ChunkProcessor로 넘어감
     * ItemReader가 읽은 item이 null일 경우 반복문 종료 및 해당 Step 반복문까지 종료
   * 기본 구현체로서 SimpleChunkProvider와 FaultTolerantChunkProvider가 있다.

> ChunkProcessor
1. 기본 개념
   * ItemProcessor를 사용해서 Item을 변형, 가공, 필터링하고 ItemWirter를 사용해서 Chunk 데이터를 저장 출력한다.
   * Chunk<O>를 만들고 앞에서 넘어온 Chunk<I>의 item을 한 건씩 처리한 후 Chunk<O>에 저장한다.
   * 외부로부터 ChunkProcessor가 호출될 때마다 항상 새로운 Chunk가 생성된다.
   * ItemProcessor는 설정 시 선택사항으로 만약 객체가 존재하지 않을 경우 ItemReader에서 읽은 item 그대로가 Chunk<O>에 저장된다.
   * ItemProcessor 처리가 완료되면 Chunk<O>에 있는 List<item>을 ItemWriter에게 전달한다.
   * ItemWriter는 Chunk size 만큼 데이터를 Commit 처리하기 때문에 Chunk size는 곧 Commit Interval이 된다.
   * 기본 구현체로서 SimpleChunkProcessor와 FaultTolerantChunkProcessor가 있다.

> ItemReader
1. 기본 개념
   * 다양한 입력으로부터 데이터를 읽어서 제공하는 인터페이스
     * 플랫(Flat) vkdlf - csv, txt(고정 위치로 정의된 데이터 필드나 특수문자로 구별된 데이터의 행)
     * xml, Json
     * DataBase
     * JMS, RabbitMQ와 같은 Message Queuing 서비스
     * Costom Reader - 구현시 멀티스레드 환경에서 스레드에 안전하게 구현할 필요가 있음
   * ChunkOrientedTasklet 실행 시 필수적 요소러 설정해야 한다.
2. 구조
    * 다수의 구현체들이 ItemReader와 ItemStream 인터페이스를 동시에 구현하고 있음
    * ExecutionContext에 read와 관련된 여러가지 상태 정보를 저장해서 재시작 시 다시 참조 하도록 

> ItemWriter
1. 기본 개념
   * Chunk 단위로 데이터를 받아 일괄 출력 작업을 위한 인터페이스
     * 플랫(Flat) 파일 - csv, txt
     * DataBase
     * JMS, RabbitMQ와 같은 Message Queuing 서비스
     * Mail Service
     * Custom Writer
   * 아이템 하나가 아닌 아이템 리스트를 전달 받는다.
   * ChunkOrientedTasklet 실행시 필수적 요소로 설정해야 한다.
2. 구조
   * void write(List<? extends T> items)
     * 출력 데이터를 아이템 리스트로 받아 처리한다.
     * 출력이 완료되고 트랜잭션이 종료되면 새로운 Chunk 단위 프로세스로 이동한다.

> ItemProcessor
1. 기본 개념
   * 데이터를 출력하기 전에 데이터를 가공, 변형, 필터링 하는 역할
   * ItemReader 및 ItemWriter와 분리되어 비즈니스 로직을 구현할 수 있다.
   * ItemReader로부터 받은 아이템들을 특정 타입으로 변호나해서 ItemWriter에 넘겨줄 수 있다.
   * ItemReader로부터 받은 아이템들 중 필터과정을 거쳐 원하는 아이템들만 ItemWriter에게 넘겨줄 수 있다.
     * ItemProcessor에서 process() 실행결과 null을 반환하면 Chunk<O>에 저장되지 않기 때문에 결국 ItemWriter에 전달되지 않는다.
   * ChunkOrientedTasklet 실행 시 선택적 요소이기 때문에 청크 기반 프로세싱에 ItemProcessor 단계가 반드시 필요한 것은 아니다.
2. 구조
   * O processor
     * `<I>` 제네릭은 ItemReader에게 받을 데이터 타입 지정
     * `<O>` 제네릭은 ItemWriter에게 보낼 데이터 타입 지정
     * 아이템 하나씩 가공 처리하며 null 리턴할 경우 해당 아이템은 Chunk<O>에 저장되지 않음
   * ItemStream을 구현하지 않는다.
   * 거의 대부분 Customizing 해서 사용하기 때문에 기본적으로 제공되는 구현체가 적다.

> ItemSteam
1. 기본 개념
   * ItemReader와 ItemWriter 처리 과정 중 상태를 저장하고 오류가 발생하면 해당 상태를 참조하여 실패한 곳에서 재시작하도록 지원.
   * 리소슬르 열고 닫아야 하며 입출력 장치 초기화 등의 작업을 해야하는 경우
   * ExecutionContext를 매개변수로 받아서 상태 정보를 업데이트 한다.
   * ItemReader, ItemWriter는 ItemStream을 구현해야 한다.
2. 구조
   * `void open(ExecutionContext executionContext) throws ItemStreamException` // read, write 메서드 호출전에 파일이나 커넥션이 필요한 리소스에 접근하도록 초기화 작업
   * `void update(ExecutionContext executionContext) throws ItemStreamException` // 현재까지 진행된 모든 상태를 저장
   * `void close() throws ItemStreamException` // 열려있는 모든 리소스를 안전하게 해제하고 닫음
***
## 스프링 배치 청크 프로세스 활용 - ItemReader
> FlatFileItemReader 개념 및 API
1. 기본 개념
   * 2차원 데이터(표)로 표현된 유형의 파일을 처리하는 ItemReader
   * 일반적으로 고정 위치로 정의된 데이터 필드나 특수 문자에 의해 구별된 데이터의 행을 읽는다.
   * `Resource`와 `LineMapper` 두 가지 요소가 필요하다.
2. 구조
`String encoding = DEFAULT_CHARSET`  // 문자열 인코딩, 디폴트는 Charset_defaultCharset()
`int linesToSkip`  // 파일 상단에 있는 무시할 라인수
`String[] comments`  // 해당 코멘트 기호가 있는 라인은 무시한다.
`Resource resource`  // 읽어야 할 리소스
`LineMapper<T> lineMapper`  // String을 Object로 변환한다.
`LineCallbackHandler skippedLinesCallback`  // 건너뛸 라인의 원래 내용을 전달하는 인터페이스 linesToSkip이 2이면 두번 호출된다.
3. Resource
   * FileSystemResource - new FileSystemResource("resource/path/config.xml")
   * ClassPathResource - new ClassPathResource("classpath:path/path/config.xml")
4. LineMapper
   * 파일의 라인 한줄을 Object로 변환해서 FlatFileItemReader로 리턴한다.
   * 단순히 문자열을 받기 때문에 문자열을 토큰화해서 객체로 매핑하는 과정이 필요하다.
   * LineTokenizer와 FieldSetMapper를 사용해서 처리한다.
   * FieldSet
     * 라인을 필드로 구분해서 만든 배열 토큰을 전달하면 토큰 필드를 참조할 수 있도록 한다.
     * JDBC의 ResultSet과 유사하다. ex) fs.readString(O), fs.readString("name")
   * LineTokenizer
     * 입력받은 라인을 FieldSet으로 변환해서 리턴한다.
     * 팡리마다 형식이 다르기 때문에 문자열을 FieldSet으로 변환하는 작업을 추상화시켜야 한다.
   * FieldSetMapper
     * FieldSet 객체를 받아서 원하는 객체로 매핑해서 리턴한다.
     * JdbcTemplate의 RowMapper와 동일한 패턴을 사용한다.
5. API
```java
public FlatFileItemReader itemReader() {
    return new FlatFileItemReaderBuilder<T>()
            .name(Strign name)                  // 이름설정 ExecutinContext 내에서 구분하기 위한 key로 저장
            .resource(Resource)                 // 읽어야 할 리소스 설정
            .delimited().delimiter("|")         // 파일의 구분자를 기준으로 파일을 읽어들이는 설정
            .fixedLength()                      // 파일의 고정길이를 기준으로 파일을 읽어들이는 설정
            .addColumns(Range)                  // 고정길이 범위를 정하는 설정
            .names(String[] fieldNames)         // LineTokenizer로 구분된 라인의 항목을 객체의 필드명과 매핑하도록 설정
            .targetType(Class class)            // 라인의 각 항목과 매핑할 객체 타입 설정
            .addComment(String Comment)         // 무시할 라인의 코멘트 기호 설정
            .strict(boolean)                    // 라인을 읽거나 토큰화 할 때 Parsing 예외가 발생하지 않도록 검증 생략하도록 설정
            .encoding(String encoding)          // 파일 인코딩 설정
            .linesToSkip(int linesToSkip)       // 파일 상단에 있는 무시할 라인 수 설정
            .saveState(boolean)                 // 상태정보를 저장할 것인지 설정
            .setLineMapper(LineMapper)          // LineMapper 객체 설정
            .setFieldSetMapper(FieldSetMapper)  // FieldSetMapper 객체 설정
            .setLineTokenizer(LineTokenizer)    // LineTokenizer 객체 설정
            .build();
}
```
> FlatFileItemReader - delimetedlinetokenizer
1. 기본 개념
   * 한 개 라인의 String을 구분자 기준으로 나누어 토큰화 하는 방식
   * 
> FlatFileItemReader - FixedLengthTokenizer
1. 기본 개념
   * 한 개 라인의 String을 사용자가 설정한 고정 길이 기준으로 나누어 토큰화 하는 방식
   * 범위는 문자열 형식으로 설정 할 수 있다.
     * "1~4" 또는 "1-3, 4-6, 7" 또는 "1-2, 4-5, 7-10"
     * 마지막 범위가 열려 있으면 나머지 행이 해당 열로 읽혀진다.

> FlatFileItemReader - Exception Handling
1. 기본 개념
    * 라인을 읽거나 토큰화 할 때 발생하는 Parsing 예외를 처리 할 수 있도록 예외 계층 제공
    * 토큰화 검증을 엄격하게 적용하지 않도록 설정하면 Parsing 예외가 발생하지 않도록 할 수 있다.
    * `FlatFileParseException`: ItemReader에서 파일을 읽어들이는 동안 발생하는 예외
    * `FlatFileFormatException`: 
      * LineTokenizer에서 토큰화 하는 도중 발생하는 예외
      * FlatFileException 보다 좀 더 구체적인 예외
    * `IncorrentTokenCountException`: `DelimitedLineTokenizer`로 토큰화 할 때 컬럼 개수와 실제 토큰화 한 컬럼 수와 다를때 발생하는 예외
    * `IncorrentLineLengthException`: `FixedLengthLineTokenizer`로 토큰화 할 때 라인 전체 길이와 컬럼 길이의 총합과 일치하지 않을 때 발생
    * 토큰화 검증 기준 설정
        1. tokenizer.setColumns(new Range[] {new Range(1,5), new Range(6, 10)}); // 토큰 길이: 10자
        2. tokenizer.setStrict(false); // 토큰화 검증을 적용하지 않음
        3. FieldSet tokens = tokenizer.tokenize("12345") // 라인 길이: 5자
        * LineTokenizer의 Strict 속성을 `false`로 설정하게 되면 Tokenizer가 라인 길이를 검증하지 않는다.
        * Tokenizer가 라인 길이나 컬럼명을 검증하지 않을 경우 예외가 발생하지 않는다.
        * FieldSet은 성공적으로 리턴이 되며 두번째 범위 값은 빈 토큰을 가지게 된다.

> XML StaxEventItemReader - 개념 및 API
* Java XML API
  * DOM 방식
    * 문서 전체를 메모리에 로드한 후 Tree 형태로 만들어서 데이터를 처리하는 방식, pull 방식
    * 엘리멘트 제어는 유연하나 문서 크기가 클 경우 메모리 사용이 많고 속도가 느림
  * SAX 방식
    * 문서의 항목을 읽을 때마다 이벤트가 발생하여 데이터를 처리하는 방식 push 방식
    * 메모리 비용이 적고 속도가 빠른 장점은 있으나 엘리멘트 제어가 어려움
  * StAX 방식 (Stream API for XML)
    * DOM과 SAX의 장점과 단점을 보완한 API 모델로서 push와 pull을 동시에 제공함
    * XML 문서를 읽고 쓸 수 있는 양방향 파서기 지원
    * XML 파일의 항목에서 항목으로 직접 이동하면서 Stax 파서기를 통해 구문 분석
    * 유형
      * Iterator API 방식
        * XMLEventReader의 nextEvent()를 호출해서 이벤트 객체를 가지고 옴
        * 이벤트 객체는 XML 태그 유형(요소, 텍스트, 주석 등)에 대한 정보를 제공함
      * Cursor API 방식
        * JDBC Resultset처럼 작동하는 API로서 XMLStreamReader는 XML 문서의 다음 요소로 커서를 이동한다.
        * 커서에서 직접 메서드를 호출하여 현재 이벤트에 대한 자세한 정보를 얻는다.
  * Spring-OXM
    * 스프링의 Object XML Mapping 기술로서 XML 바인딩 기술을 추상화함
      * Marshaller 
        * marshall - 객체를 XML로 직렬화 하는 행위
      * Unmarshaller
        * unmarshall - XML을 객체로 역직렬화하는 행위
      * Marshaller와 Unmarshaller 바인딩 기능을 제공하는 오픈소스로 JaxB2, Castor, XmlBeans, Xstream 등이 있다.
    * 스프링 배치는 특정한 XML 바인딩 기술을 강요하지 않고 Spring OXM에 위임한다.
      * 바인딩 기술을 제공하는 구현체를 선택해서 처리하도록 한다.
  * Spring Batch XML
    * 스프링 배치에서는 StAX 방식으로 XML 문서를 처리하는 StaxEventItemReader를 제공한다.
    * XML을 읽어 자바 객체로 매핑하고 자바 객체를 XML로 쓸 수 있는 트랙잭션 구조를 지원
  
  * StAX 아키텍처
    * XML 전체 문서가 아닌 조각 단위로 구문을 분석하여 처리할 수 있다.
    * 루트 엘리먼트 사이에 있는 것들은 전부 하나의 조각(Fragment)을 구성한다.
    * 조각을 읽을 때 DOM의 pull 방식을 사용하고 조각을 객체로 바인딩 처리하는 것은 SAX의 push 방식을 사용한다.

```java
public StaxEventItemReader itemReader() {
    return new StaxEventItemReaderBuilder<T> ()
            .name(String name)
            .resource(Resource)                                 // 읽어야할 리소스 설정
            .addFragmentRootElements(String ...rootElements)    // Fragment 단위의 루트 엘리먼트 설정, 이 루트 조각 단위가 객체와 매핑하는 기준
            .unmarshaller(Unmarshaller)                         // Unmarshaller 객체 설정
            .saveState(boolean)                                 // 상태 정보 저장여부 설정, 기본값은 true
            .build();
}
```

> StaxEventItemReader
1. 기본 개념
    * Stax API 방식으로 데이터를 읽어 들이는 ItemReader
    * Spring-OXM과 Xstream 의존성을 추가해야 한다.

> JsonItemReader
1. 기본 개념
   * Json 데이터의 Parsing과 Binding을 JsonObjectReader 인터페이스 구현체에 위임하여 처리하는 itemReader
   * 두가지 구현체 제공
     * JacksonJsonObjectReader
     * GsonJsonObjectReader

> Cursor Based & Paging Based
1. 기본 개념
    * 배치 애플리케이션은 실시간 처리가 어려운 대용량 데이터를 다루며 이 때 DB I/O의 성능 문제와 메모리 자원의 효율성 문제를 해결할 수 있어야 한다.
    * 스프링 배치에서는 대용량 데이터 처리를 위한 두가지 해결방안을 제시하고 있다.
2. Cursor Based 처리
    * JDBC ResultSet의 기본 메커니즘을 사용
    * 현재 행에 커서를 유지하며 다음 데이터를 호출하면 다음 행으로 커서를 이동하며 데이터 반환이 이루어지는 Streaming 방식의 I/O이다.
    * ResultSet이 open 될 때마다 next() 메소드가 호출 되어 Database의 데이터가 반환되고 객체와 매핑이 이루어진다.
    * DB connection이 연결되면 배치 처리가 완료될 때 까지 데이터를 읽어오기 때문에 DB와 SocketTimeout을 충분히 큰 값으로 설정 필요.
    * 모든 결과를 메모리에 항당하기 때문에 메모리 사용량이 많아지는 단점이 있다.
    * Connection 연결 유지 시간과 메모리 공간이 충분하다면 대량의 데이터 처리에 적합할 수 있다.(fetchSize 조절)
3. Paging Based 처리
    * 페이징 단위로 데이터를 조회하는 방식으로 Page Size 만큼 한번에 메모리에 가지고 온 다음 한 개씩 읽는다.
    * 한 페이지를 읽을 때마다 Connection을 맺고 끊기 때문에 대량의 데이터를 처리하더라도 SokectTimeout 예외가 거의 일어나지 않는다.
    * 시작 행 번호를 지정하고 페이지에 반환시키고자 하는 행의 수를 지정한 후 사용 - Offset, Limit
    * 페이징 단위의 결과만 메모리에 할당하기 때문에 메모리 사용량이 적어지는 장점이 있다.
    * Connection 연결 유지 시간이 길지 않고 메모리 공간을 효율적으로 사용해야하는 데이터 처리에 적합할 수 있다.

> JDBC CursorItemReader
1. 기본 개념
    * Cursor 기반의 JDBC 구현체로서 ResultSet과 함께 사용되며 DataSource에서 Connection을 얻어와서 SQL을 실행한다.
    * Thread 안정성을 보장하지 않기 때문에 멀티 스레드 환경에서 사용할 경우 동시성 이슈가 발생하지 않도록 별도 동기화 처리가 필요하다.
2. API
```java
public JdbcCursorItemReader<T>() {
    return new JdbcCursorItemReaderBuilder<T>()
        .name("cursorItemReader")           
        .fetchSize(int chunkSize)           // Cursor 방식으로 데이터를 가지고 올 때 한번에 메모리에 할당할 크기를 설정한다.
        .dataSource(DataSource)             // DB에 접근하기 위해 DataSource 설정
        .rowMapper(RowMapper)               // 쿼리 결과로 반환되는 데이터와 객체를 매핑하기 위한 RowMapper 설정
        .beanRowMapper(Class<T>)            // 별도의 RowMapper를 설정하지 않고 클래스 타입을 설정하면 자동으로 객체와 매핑
        .sql(String sql)                    // ItemReader가 조회 할 때 사용할 쿼리 문장 설정
        .queryArguments(Object... args)     // 쿼리 파라미터 설정
        .maxItemCount(int count)            // 조회 할 최대 item 수
        .currentItemCount(int count)        // 조회 item의 시작 시점
        .maxRows(int maxRows)               // ResultSet 오브젝트가 포함 할 수 있는 최대 행 수
        .build();
}
```
> JpaCursorItemReader
1. 기본 개념
    * SpringBatch 4.3버전부터 지원
    * Cursor 기반의 JPA 구현체로서 EntityManagerFactory 객체가 필요하며 쿼리는 JPQL을 사용한다.
2. API
```java
public JpaCursorItemReader itemReader() {
    return new JpaCursorItemReaderBuilder<T>()
            .name("customItemReader")
            .queryString(String JPQL)                           // ItemReader가 조회 할 때 사용할 JPQL 문장 설정
            .EntityManagerFactory(EntityManagerFactory)         // JPQL을 실행하는 EntityManager를 생성하는 팩토리
            .parameterValue(Map<String, Object> parameters)     // 쿼리 파라미터 설정
            .maxItemCount(int count)                            // 조회 할 최대 item 수
            .currentItemCount(int count)                        // 조회 Item의 시작 지점
            .build();
}
```

> JDBCPagingItemReader
1. 기본 개념
   * Paging 기반의 JDBC 구현체로서 쿼리에 시작 행 번호(Offset)와 페이지에서 반환 할 행 수(Limit)를 지정해서 SQL을 실행한다.
   * 스프링 배치에서 offset과 limit을 PageSize에 맞게 자동으로 생성해 주며 페이징 단위로 데이터를 조회할 때마다 새로운 쿼리가 실행한다.
   * 페이지마다 새로운 쿼리를 실행하기 때문에 페이징 시 결과 데이터의 순서가 보장될 수 있도록 order by 구문이 작성되도록 한다.
   * 멀티 스레드 환경에서 Thread 안정성을 보장하기 때문에 별도의 동기화를 할 필요가 없다.
   * PagingQueryProvider
     * 쿼리 실행에 필요한 쿼리문을 ItemReader에게 제공하는 클래스
     * 데이터베이스마다 페이징 전략이 다르기 때문에 각 데이터베이스 유형마다 다른 PaingQueryProvider를 사용한다.
     * Select절, From절, sortKey는 필수로 설정해야 하며 where, group by절은 필수가 아니다.
2. API
```java
public JdbcPagingItemReader itemReader() {
    return new JdbcPagingItemReaderBuilder<T>()
            .name("pagingItemReader")                           
            .pageSize(int pageSize)                             // 페이지 크기 설정(쿼리 당 요청할 레코드 수)
            .dataSource(DataSource)                             // DB에 접근하기 위해 DataSource 설정
            .queryProvider(PagingQueryProvider)                 // DB 페이징 전략에 따른 PagingQueryProvider 설정
            .rowMapper(Class<T>)                                // 쿼리 결과로 반환되는 데이터와 객체를 매핑하기 위한 RowMapper 설정
            .selectClause(String selectClause)                  // select절 설정
            .fromClause(Strinf fromClause)                      // from절 설정
            .where(String whereClause)                          // where절 설정
            .groupClause(String groupClause)                    // group절 설정
            .sortKey(Map<String, Object> sortKeys)              // 정렬을 위한 유니크한 키 설정
            .parameterValues(Map<String, Object> paramters)     // 쿼리 파라미터 설정
            .maxItemCount(int Count)                            // 조회 할 최대 item 수
            .currentItemCount(int count)                        // 조회 item의 시작 지점
            .maxRows(int maxRows)                               // resultSet 오브젝트가 포함 할 수 있는 최대 행 수
            .build();
}
```
> JpaPagingItemReader
1. 기본 개념
    * Paging 기반의 JPA 구현체로서 EntityManagerFactory 객체가 필요하며 쿼리는 JPQL을 사용한다.
2. API
```java
public JpaPagingItemReader itemrReaer() {
    return new JpaPagingItemReaderBuilder<T>()
            .name("pagingItemReader")
            .pageSize(int count)                                // 페이지 크기 설정 (쿼리 당 요청할 레코드 수)
            .queryString(String JPQL)                           // itemReader가 조회 할 때 사용할 JPQL 문장 설정
            .EntityManagerFactory(EntityManagerFactory)         // JPQL을 실행하는 EntityManager를 생성하는 팩토리
            .parameterValue(Map<String, Object> parameters)     // 쿼리 파라미터 설정
            .build();
}
```

> ItemReaderAdapter
1. 기본 개념
    * 배치 Job 안에서 이미 있는 DAO나 다른 서비스를 ItemReader 안에서 사용하고자 할 때 위임 역할을 한다.
***
## 스프링 배치 청크 프로세스 활용 - ItemWriter
1. 기본 개념
   * 2차원 데이터(표)로 표현된 유형의 파일을 처리하는 ItemWriter
   * 고정 위치로 정의된 데이터 필드나 특수 문자에 의해 구별된 데이터의 행을 기록한다.
   * Resource와 LineAggregator 두 가지가 요소가 필요하다.
2. 구조
> FlatFileItemWriter
>
> String encoding = DEFAULT_CHARSET // 문자열 인코딩, 디폴트는 Charset.defaultCharset()
> 
> boolean append = false // 대상 파일이 이미 있는 경우 데이터를 계속 추가할 것인지 여부
> 
> Resource resource  // 작성해야 할 리소스
> 
> LineAggregator<T> lineAggregator
> 
> FlatFileHeaderCallback headerCallback // 헤더를 파일에 쓰기 위한 콜백 인터페이스
> 
> FlatFileFooterCallback footerCallback // 푸터를 파일에 쓰기 위한 콜백 인터페이스

* LineAggregator
  * Item을 받아서 String으로 변환하여 리턴한다
  * FieldExtractor를 사용해서 처리할 수 있다
  * 구현체
    * PassThroughLineAggregator, DelimitedLineAggregator, FormatterLineAggregator
* FieldExtractor
  * 전달 받은 Item 객체의 필드를 배열로 만들고 배열을 합쳐서 문자열을 만들도록 구현하도록 제공하는 인터페이스
  * 구현체
    * BeanWrapperFieldExtractor, PassThroughFieldExtractor

```java
public FlatFileItemWriter itemWriter() {
    return new FlatFileItemWriterBuilder<T>()
            .name(String name)
            .resource(Resource)                         // 쓰기할 리소스 설정
            .lineAggregator(LineAggregator<T>)          // 객체를 String으로 변환하는 LineAggregator 객체 설정
            .append(boolean)                            // 존재하는 파일에 내용을 추가할 것인지 여부 설정
            .fieldExtractor(FiledExtractor<T>)          // 객체 필드를 추출해서 배열로 만드는 FeildExtractor 설정
            .headerCallback(FlatFileHeaderCallback)     // 헤더를 파일에 쓰기위한 콜백 인터페이스 
            .footerCallback(FlatFileFooterCallback)     // 푸터를 파일에 쓰기위한 콜백 인터페이스
            .shouldDeleteifExists(boolean)              // 파일이 이미 존재한다면 삭제
            .shouldDeleteIfEmpty(boolean)               // 파일의 내용이 비어 있다면 삭제 
            .delimited().delimiter(String delimiter)    // 파일의 구분자를 기준으로 파일을 작성하도록 설정
            .formatted().format(String format)          // 파일의 고정길이를 기준을 파일을 작성하도록 설정
            .build();
}
```

> FlatFileItemWriter - delimitedLineAggregator
* 기본 개념
  * 객체의 필드 사이에 구분자를 삽입해서 한 문자열로 변환한다.

> FlatFileItemWriter - FormatterLineAggregator
* 기본 개념
  * 객체의 필드를 사용자가 설정한 Formatter 구문을 통해 문자열로 변환한다.

> XML StaxEventItemWriter
* 기본 개념
  * XML 쓰는 과정은 읽기 과정에 대칭적이다.
  * StaxEventItemWriter는 Resource, marshaller, rootTagName이 필요하다.
* API
```java
public StaxEventItemReader itemReader() {
    return new StaxEventItemWriterBuilder<T>()
            .name(String name)
            .resource(Resource)         // 쓰기할 리소스 설정
            .rootTagName()              // 조각 단위의 루트가 될 이름 설정
            .overwriteOutPut(boolean)   // 파일이 존재하면 엎어 쓸 것인지 설정
            .marshaller(Marshaller)     // Marshaller 객체 설정
            .headerCallback()           // 헤더를 파일에 쓰기 위한 콜백 인터페이스
            .footerCallback()           // 푸터를 파일에 쓰기 위한 콜백 인터페이스
            .build();
}
```

> Json FileItemWriter
* 기본 개념
  * 객체를 받아 Json String으로 변환하는 역할을 한다.
* API
```java
public JsonFileItemWriter itemWriter() {
    return new JsonFileItemWirterBuilder<T> ()
            .name(String name)
            .resource(Resource)                             // 쓰기할 리소스 설정
            .append(boolean)                                // 존재하는 파일에 내용을 추가 여부 설정
            .jsonObjectMarshaller(JsonObjectMarshaller)     // JsonObjectMarshaller 객체 설정
            .headerCllback(FlatFileHeaderCallback)          // 헤더를 파일에 쓰기 위한 콜백 인터페이스
            .footerCallback(FlatFileFooterCallback)         // 푸터를 파일에 쓰기 위한 콜백 인터페이스
            .shouldDeleteIfExists(boolean)                  // 파일이 이미 존재한다면 삭제 여부 설정
            .shouldDeleteIfEmpty(boolean)                   // 파일의 내용이 비어있다면 삭제 여부 설정
            .build();
            
}
```
> JdbcBatchItemWriter
* 기본 개념
  * JdbcCursorItemReader 설정과 마찬가지로 datasource를 지정하고 SQL속성에 실행할 쿼리를 설정
  * JDBC의 Batch 기능을 사용하여 bulk insert/update/delete 방식으로 처리
  * 단건 처리가 아닌 일괄처리이기 때문에 성능에 이점을 가진다.
* API
```java
public JdbcBatchItemWriter itemWriter() {
    return new JdbcBatchItemWriterBuilder<T>()
            .name(String name)         
            .datasrouce(Datasource)     // DB에 접근하기 위해 Datasource 설정
            .sql(String sql)            // ItemWriter가 사용할 쿼리 문장 설정
            .assertUpdates(boolean)     // 트랜잭션 이후 적어도 하나의 항목이 행을 업데이트 혹은 삭제하지 않을 경우 예외방생여부를 설정함, 기본값은 true
            .beanMapped()               // Pojo 기반으로 Insert SQL의 Values를 매핑
            .columnMapped()             // key, value 기반으로 Insert SQL의 Values를 매핑
            .build();
}
```
> JpaItemWriter
* 기본 개념
  * JPA Entity 기반으로 데이터를 처리하며 EntityManagerFactory를 주입받아 사용한다.
  * Entity를 하나씩 chunk 크기 만큼 insert 혹은 merge 한다음 flush 한다.
  * ItemReader나 ItemProcessor로부터 아이템을 전달 받을 때는 Entity 클래스 타입으로 받아야 한다.
* API
```java
public JpaItemWriter itemWriter() {
    return new JpaItemWriterBuilder<T>()
            .usePersis(boolean)                             // Entity를 persist() 할 것인지 여부 설정, false이면 merge() 처리
            .entityManagerFactory(EntityManagerFactory)     // EntityManagerFactory 설정
            .build();
}
```
> ItemWriterAdapter
* 기본 개념
  * 배치 Job안에서 이미 있는 DAO나 다른 서비스를 ItemWriter안에서 사용하고자 할 때 위임 역할을 한다.
***
## 스프링 배치 청크 프로세스 활용 - ItemProcessor
> CompositeItemProcessor
* 기본 개념
    * ItemProcessor들을 연결(Chaining)해서 위임하면 각 ItemProcessor를 실행시킨다.
    * 이전 ItemProcessor 반환 값은 다음 ItemProcessor 값으로 연결된다.
* API
```java
public ItemProcessor itemProcessor() {
    return new CompositeItemProcessorBuilder<T>()
            .deletages(ItemProcessor<?, ?>... deletages)    // 체이닝 할 ItemProcessor 객체 설정
            .build();
}
```
> ClassifierCompositeItemProcessor
* 기본 개념
  * Classifier로 라우팅 패턴을 구현해 ItemProcessor 구현체 중에서 하나를 호출하는 역할을 한다.
* API
```java
public ItemProcessor itemProcessor() {
    return new ClassifierCompositeItemProcessorBuilder<>()
            .classifier(Classifier)     // 분류자 설정
            .build();
}
```
***
## 스프링 배치 반복 및 오류 제어
> Repeat
* 기본 개념
  * Spring Batch는 얼마나 작업을 반복해야 하는지 알려 줄 수 있는 기능을 제공한다.
  * 특정 조건이 충족 될 때까지 (또는 특정 조건이 아직 충족되지 않을 때까지) Job 또는 Step을 반복하도록 배치 애플리케이션을 구성할 수 있다.
  * 스프링 배치에서는 Step의 반복과 Chunk 반복을 RepeatOperation을 사용해서 처리하고 있다.
  * 기본 구현체로 RepeatTemplate를 제공한다.
* 구조
  * Job -> Step -> [RepeatTemplate -> Tasklet -> [RepeatTemplate -> Chunk]]
* 반복을 종료할 것인지 여부를 결정하는 세가지 항목
  * RepeatStatus
    * 스프링 배치의 처리가 끝났는지 판별하기 위한 열거형(enum)
      * CONTINUABLE - 작업이 남아 있음
      * FINISHED - 더 이상의 반복 없음
  * CompletionPolicy
    * RepeatTemplate의 iterate 메소드 안에서 반복을 중단할지 결정
    * 실행 횟수 또는 완료시기, 오류 발생시 수행 할 작업에 대한 반복여부 결정
    * 정상 종료를 알리는데 사용된다.
  * ExceptionHandler
    * RepeatCallback 안에서 예외가 발생하면 RepeateTemplate가 ExceptionHandler를 참조해서 예외를 다시 던질지 여부 결정
    * 예외를 받아서 다시 던지게 되면 반복 종료
    * 비정상 종료를 알리는데 사용된다.
> FaultTolerant
* 기본 개념
    * 스프링 배치는 job 실행 중에 오류가 발생할 경우 장애를 처리하기 위한 기능을 제공하며 이를 통해 복원력을 향상시킬 수 있다.
    * 오류가 발생해도 Step 이 즉시 종료되지 않고 Retry 혹은 Skip 기능을 활성화 함으로써 내결함성 서비스가 가능하도록 한다.
    * 프로그램의 내결함성을 위해 Skip과 Retry 기능을 제공한다.
      * Skip
        * ItemReader / ItemProcessor / ItemWriter에 적용할 수 있다.
      * Retry
        * ItemProcessor / ItemWriter에 적용할 수 있다.
      * FaultTolerant 구조는 청크 기반의 프로세스 기반위에 Skip과 Retry 기능이 추가되어 재정의 되어 있다.
* API
```java
public Step batchStep() {
    return new StepBuilderFactory.get("batchStep")
            .<I, O>chunk(10)
            .reader(ItemReader)
            .writer(ItemWriter)
            .falutTolerant()                                // 내결함성 기능 활성화
            .skip(Class<? extends Throwable> type)          // 예외 발생시 Skip할 예외 타입 설정
            .skipLimit(int skipLimit)                       // skip 제한 횟수 설정
            .noSkip(Class<? extends Throwable> type)        // skip을 어떤 조건과 기준으로 적용 할 것인지 정책 설정
            .retry(Class<? extends Throwable> type)         // 예외 발생 시 Skip 하지 않을 예외 타입 설정
            .retryLimit(int retryLimit)                     // 예외 발생 시 Retry 할 예외 타입 설정
            .retryPolicy(RetryPolicy retryPolicy)           // Retry를 어떤 조건과 기준으로 적용할 것인지 정책 설정
            .backOffPolicy(BackOffPlicy backOffPolicy)      // 다시 Retry하기까지의 지연시간(단위:ms)을 설정
            .noRetry(Class<? extends Throwable> type)       // 예외 발생 시 Retry 하지 않을 예외 타입 설정 
            .noRollback(Class<? extends Throwable> type)    // 예외 발생 시 Rollback 하지 않을 예외 타입설정
            .build();
}
```
> Skip
* Skip은 데이터를 처리하는 동안 설정된 Exception이 발생했을 경우, 해당 데이터 처리를 건너뛰는 기능이다.
* 데이터의 사소한 오류에 대해 Step의 실패처리 대신 Skip을 함으로써, 배치 수행의 빈번한 실패를 줄일 수 있게 한다.
* Skip 기능은 내부적으로 SkipPolicy를 통해 구현되어 있다.
* Skip 기능 여부를 판별하는 기준은 다음과 같다.
  1. Skip 대상에 포함된 예외인지 여부
  2. Skip 카운터를 초과 했는지 여부
> SkipPolicy
* 스킵 정책에 따라 아이템의 Skip 여부를 판단하는 클래스
* 스프링 배치가 기본적으로 제공하는 SkipPolicy 구현체들이 있으며 필요시 직접 생성해서 사용할 수 있다. 그리고 내부적으로 Classfier 클래스들을 활용하고 있다.
* 종류 
  * AlwaysSkipItemSkipPlicy: 항상 Skip
  * ExceptionClassifierSkipPolicy: 예외 대상을 분류하여 Skip 여부를 결정한다.
  * CompositeSkipPolicy: 여러 SkipPolicy를 탐색하면서 SKip 여부를 결정한다.
  * LimitCheckingItemSkipPolicy: Skip 카운터 및 예외 등록 결과에 따라 Skip 여부를 결정한다.
  * NeverSkipItemSkipPolicy: Skip을 하지 않는다.
```java
public Step batchStep() {
    return stepBuilderFactory.get("batchStep")
            .<I, O>chunk(10)
            .reader(ItemReader)
            .writer(ItemWriter)
            .faultTolerant()
            .skip(Class<? extends Throwable type> type)     // 예외 발생 시 Skip 할 예외 타입 설정
            .skipLimit(int skipLimit)                       // Skip 제한 횟수 설정, ItemReader, ItemProcessor, ItemWriter 횟수 합이다.
            .skipPolicy(SkipPolicy skipPolicy)              // Skip을 어떤 조건과 기준으로 적용할 것인지 정책 설정
            .noSkip(Class<? extends Throwable> type)        // 예외 발생 시 Skip 하지 않을 예외 타입 설정
            .build();
}
```
> Retry
* 기본 개념
  * Retry는 ItemProcessor, ItemWriter에서 설정된 Exeception이 발생 했을 경우, 지정한 정책에 따라 데이터 처리를 재시도하는 기능이다.
  * Skip과 마찬가지로 Retry를 함으로써, 배치수행의 빈번한 실패를 줄일 수 있게 한다.
* Retry 기능은 내부적으로 RetryPolicy를 통해 구현되어있다
* Retry 가능 여부를 판별하는 기준은 다음과 같다
  1. 재시도 대상에 포함된 예외인지 여부
  2. 재시도 카운터를 초과했는지 여부
> RetryPolicy
* 재시도 정책에 따라 아이템의 retry 여부를 판단하는 클래스
* 기본적으로 제공하는 RetryPolicy 구현체들이 있으며 필요시 직접 생성해서 사용할 수 있다.
  * AlwaysRetryPolicy: 항상 재시도를 허용한다.
  * ExceptionClassifierRetryPolicy: 예외대상을 분류하여 재시도 여부를 결정한다.
  * CompositeRetryPolicy: 여러 RetryPolicy를 탐색하면서 재시도 여부를 결정한다.
  * SimpleRetryPolicy: 재시도 횟수 및 예외 등록 결과에 따라 재시도 여부를 결정한다. 기본값으로 설정된다.
  * MaxAttemptRetryPolicy: 재시도 회숫에 따라 재시도 여부를 결정한다.
  * TimeoutRetryPolicy: 주어진 시간동안 재시도를 허용한다.
  * NeverRetryPolicy: 최초 한번만 허용하고 그 이후로는 허용하지 않는다.
```java
public Step batchStep() {
    return stepBuilderFactory.get("batchStep")
            .<I, O>chunk(10)
            .reader(ItemReader)
            .writer(ItemWriter)
            .faultTolerant()
            .retry(Class<? extends Throwable> type)         // 에외 발생시 Retry할 예외 타입 설정
            .retryLimit(int retryLimit)                     // Retry 제한 횟수 설정
            .retryPolicy(SkipPolicy skipPolicy)             // Retry를 어떤 조건과 기준으로 적용 할 것인지 정책 설정
            .noRetry(Class<? extends Throwable> type)       // 예외 발생시 Retry 하지 않을 예외 타입 설정
            .backOffPolicy(BackOffPolicy backOffPolicy)     // 다시 Retry하기까지의 지연시간(단위:ms)을 설정
            .noRollback(Class<? extends Throwable> type)    // 예외 발생시 Rollback 하지 않을 예외타입 설정
            .build();
}
```
***
## 스프링 배치 멀티 스레드 프로세싱
> 기본 개념
* 단일 스레드 vs 멀티 스레드
  * 프로세스 내 특정 작업을 처리하는 스레드가 하나일 경우 단일 스레드, 여러 개 일 경우 멀티스레드로 정의할 수 있다.
  * 작업 처리에 있어서 단일 스레드와 멀티 스레드의 선택 기준은 어떤 방식이 자원을 효율적으로 사용하고 성능처리에 유리한가 하는 점이다.
  * 일반적으로 복잡한 처리나 대용량 데이터를 다루는 작업일 경우 전체 소요 시간 및 성능상의 이점을 가져오기 위해 멀티 스레드 방식을 선택한다.
  * 멀티 스레드 처리 방식은 데이터 동기화 이슈가 존재하기 때문에 최대한 고려해서 결정해야 한다.
* 스프링 배치 스레드 모델
  * 스프링 배치는 기본적으로 단일 스레드 방식으로 작업을 처리한다.
  * 성능 향상과 대규모 데이터 작업을 위한 비동기 처리 및 Scale out 기능을 제공한다.
  * Local과 Remote 처리를 지원한다.
  
  1. AsyncItemProcessor / AsyncItemWriter
     * ItemProcessor에게 별도의 스레드가 할당 되어 작업을 처리하는 방식
  2. Multi-thread Step
     * Step 내 Chunk 구조인 ItemReader, ItemProcessor, ItemWriter 마다 여러 스레다가 할당되어 실행하는 방법
  3. Remote Chunking
     * 분산환경처럼 Step 처리가 여러 프로세스로 분할되어 외부의 다른 서버로 전송되어 처리되는 방식
  4. Parallel Steps
     * Step 마다 스레드가 할당되어 여러개의 Step을 병렬로 실행하는 방법
  5. Partitioning
     * Master / Slave 방식으로 Master가 데이터를 파티셔닝 한 다음 각 파티션에게 스레드를 할당하여 Slave가 독립적으로 작동하는 방식
> AsyncItemProcessor / AsyncItemWriter
* 기본 개념
  * Step 안에서 ItemProcessor가 비동기적으로 동작하는 구조
  * AsyncItemProcessor와 AsyncItemWriter가 함께 구성이 되어야 함
  * AsyncItemProcessor로부터 AsyncItemWriter가 받는 최종 결과값은 List<Future<T>>타입이며 비동기 실행이 완료 될 때까지 대기한다.
  * spring-batch-intergration 의존성이 필요하다.
```java
public Step step() throws Exception {
    return stepBuilderFactory.get("step")
            .<I, O>chunk(100)
            .reader(pagingItemReader())         // ①
            .processor(asyncItemProcessor())    // ②
            .writer(asyncItemWriter())          // ③
            .build()
}
```
① ItemReader설정: 비동기 실행 아님
② 비동기 실행을 위한 AsyncItemProcessor 설정
    - 청크 개수 혹은 스레드 풀 개수 만큼 스레드가 생성되어 비동기로 실행된다.
    - 내부적으로 실제 ItemProcessor에게 실행을 위임하고 결과를 Future에 저장한다.
③ AsyncItemWriter 설정
    - 비동기 실행 결과 값들을 모두 받아오기까지 대기한다.
    - 내부적으로 실제 ItemWriter에게 최종 결과값을 넘겨주고 실행을 위임한다.
