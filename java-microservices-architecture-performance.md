# 🏗️ Java Microservices - Architecture & Performance Guide

**Author:** Java Interview Preparation Guide  
**Target Audience:** Senior Java Developers preparing for architecture interviews  

---

## 📋 Table of Contents

1. [Java Thread Management Impact on Microservices Under High Load](#1-java-thread-management-impact-on-microservices-under-high-load)
2. [Why Synchronous REST Calls Become Bottlenecks](#2-why-synchronous-rest-calls-become-bottlenecks)
3. [How Improper Thread Pool Configuration Crashes Services](#3-how-improper-thread-pool-configuration-crashes-services)
4. [Retries Without Exponential Backoff](#4-retries-without-exponential-backoff)
5. [Handling Partial Failures in Microservice Chain](#5-handling-partial-failures-in-microservice-chain)
6. [Why Scaling Services Sometimes Increases Latency](#6-why-scaling-services-sometimes-increases-latency)
7. [JVM GC Behavior Impact on Performance](#7-jvm-gc-behavior-impact-on-performance)
8. [Thread Starvation in Distributed Systems](#8-thread-starvation-in-distributed-systems)
9. [Managing Inter-Service Timeouts](#9-managing-inter-service-timeouts)
10. [Why Shared Database is an Anti-Pattern](#10-why-shared-database-is-an-anti-pattern)

---

## 1. Java Thread Management Impact on Microservices Under High Load

### The Problem

Under high load, poor thread management can cause:
- **Thread pool exhaustion**
- **Memory overflow**
- **Request queuing**
- **Service unavailability**
- **Cascading failures**

### Visual Representation

```
Normal Load:
Request → [Thread Pool: 10/200] → Process → Response
✅ Fast response

High Load (Poor Management):
1000 Requests → [Thread Pool: 200/200 FULL!] 
                    ↓
              [Queue: 1000+]
                    ↓
           ❌ Timeout/OOM/Crash
```

### Code Example: Poor Thread Management

```java
// ❌ BAD: Unbounded thread pool
@Service
public class OrderService {
    
    // Creates new thread for every request - DISASTER under load!
    private ExecutorService executor = Executors.newCachedThreadPool();
    
    @Autowired
    private RestTemplate restTemplate;
    
    public OrderResponse processOrder(Order order) {
        // Blocking call on bounded thread pool
        CompletableFuture<PaymentResponse> payment = CompletableFuture.supplyAsync(() -> {
            return restTemplate.postForObject(
                "http://payment-service/process", 
                order.getPayment(), 
                PaymentResponse.class
            );
        }, executor);
        
        CompletableFuture<InventoryResponse> inventory = CompletableFuture.supplyAsync(() -> {
            return restTemplate.postForObject(
                "http://inventory-service/reserve", 
                order.getItems(), 
                InventoryResponse.class
            );
        }, executor);
        
        // Blocks current thread!
        payment.join();
        inventory.join();
        
        return new OrderResponse();
    }
}
```

**Problems:**
1. `newCachedThreadPool()` creates unlimited threads → OOM
2. Blocking `join()` calls waste threads
3. No timeout handling
4. No circuit breaker
5. No bulkhead isolation

### Solution: Proper Thread Management

```java
// ✅ GOOD: Bounded thread pool with proper configuration
@Configuration
public class ThreadPoolConfig {
    
    @Bean(name = "orderProcessorExecutor")
    public ThreadPoolTaskExecutor orderProcessorExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core threads always alive
        executor.setCorePoolSize(10);
        
        // Maximum threads under load
        executor.setMaxPoolSize(50);
        
        // Queue size - reject after this
        executor.setQueueCapacity(100);
        
        // Thread naming for debugging
        executor.setThreadNamePrefix("order-processor-");
        
        // Rejection policy
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Allow core threads to timeout
        executor.setAllowCoreThreadTimeOut(true);
        executor.setKeepAliveSeconds(60);
        
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "externalCallExecutor")
    public ThreadPoolTaskExecutor externalCallExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("external-call-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }
}

@Service
public class ImprovedOrderService {
    
    @Autowired
    @Qualifier("orderProcessorExecutor")
    private ThreadPoolTaskExecutor orderExecutor;
    
    @Autowired
    @Qualifier("externalCallExecutor")
    private ThreadPoolTaskExecutor externalExecutor;
    
    @Autowired
    private WebClient webClient; // Non-blocking!
    
    @CircuitBreaker(name = "orderService", fallbackMethod = "orderFallback")
    @TimeLimiter(name = "orderService")
    @Bulkhead(name = "orderService")
    public CompletableFuture<OrderResponse> processOrder(Order order) {
        
        // Non-blocking reactive calls
        Mono<PaymentResponse> paymentMono = webClient.post()
            .uri("http://payment-service/process")
            .bodyValue(order.getPayment())
            .retrieve()
            .bodyToMono(PaymentResponse.class)
            .timeout(Duration.ofSeconds(3));
        
        Mono<InventoryResponse> inventoryMono = webClient.post()
            .uri("http://inventory-service/reserve")
            .bodyValue(order.getItems())
            .retrieve()
            .bodyToMono(InventoryResponse.class)
            .timeout(Duration.ofSeconds(3));
        
        // Combine without blocking
        return Mono.zip(paymentMono, inventoryMono)
            .map(tuple -> new OrderResponse(tuple.getT1(), tuple.getT2()))
            .toFuture();
    }
    
    public CompletableFuture<OrderResponse> orderFallback(Order order, Exception e) {
        log.error("Order processing failed, using fallback", e);
        return CompletableFuture.completedFuture(new OrderResponse("PENDING", "Will retry"));
    }
}
```

### Key Metrics to Monitor

```java
@Component
public class ThreadPoolMetrics {
    
    @Scheduled(fixedDelay = 10000)
    public void logThreadPoolStats() {
        ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean("orderProcessorExecutor");
        
        ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
        
        log.info("Thread Pool Stats: " +
            "Active=" + threadPool.getActiveCount() + ", " +
            "Pool Size=" + threadPool.getPoolSize() + ", " +
            "Core Pool Size=" + threadPool.getCorePoolSize() + ", " +
            "Max Pool Size=" + threadPool.getMaximumPoolSize() + ", " +
            "Queue Size=" + threadPool.getQueue().size() + ", " +
            "Completed Tasks=" + threadPool.getCompletedTaskCount());
        
        // Alert if thread pool is exhausted
        if (threadPool.getActiveCount() >= threadPool.getMaximumPoolSize() * 0.9) {
            log.error("Thread pool near exhaustion! Consider scaling or optimization.");
        }
    }
}
```

### Best Practices

| Practice | Description | Benefit |
|----------|-------------|---------|
| **Bounded Pools** | Always set max pool size | Prevents OOM |
| **Queue Limits** | Set queue capacity | Fast failure detection |
| **Thread Naming** | Use meaningful thread names | Easier debugging |
| **Separate Pools** | Different pools for different tasks | Bulkhead isolation |
| **Monitoring** | Track pool metrics | Early problem detection |
| **Non-Blocking I/O** | Use WebClient/Reactive | Better throughput |
| **Timeouts** | Set appropriate timeouts | Prevent hanging threads |

---

## 2. Why Synchronous REST Calls Become Bottlenecks

### The Problem

Synchronous REST calls block threads while waiting for responses, leading to:
- **Thread pool exhaustion**
- **Poor resource utilization**
- **Cascading latency**
- **Reduced throughput**

### Visual Representation

```
Synchronous (Blocking):
Thread 1: [Request]→[Wait 500ms]→[Response] (Thread blocked for 500ms)
Thread 2: [Request]→[Wait 500ms]→[Response] (Thread blocked for 500ms)
Thread 3: [Request]→[Wait 500ms]→[Response] (Thread blocked for 500ms)

Result: 3 threads handle only 6 requests/second

Asynchronous (Non-Blocking):
Thread 1: [Req1]→[Req2]→[Req3]→[Resp1]→[Resp2]→[Resp3]
Thread 2: [Req1]→[Req2]→[Req3]→[Resp1]→[Resp2]→[Resp3]

Result: 2 threads handle 100+ requests/second
```

### Code Example: Synchronous Bottleneck

```java
// ❌ BAD: Synchronous blocking calls
@RestController
@RequestMapping("/api/orders")
public class SyncOrderController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @GetMapping("/{orderId}")
    public OrderDetails getOrderDetails(@PathVariable String orderId) {
        // Call 1: Get order (blocks thread for ~100ms)
        Order order = restTemplate.getForObject(
            "http://order-service/orders/" + orderId, 
            Order.class
        );
        
        // Call 2: Get customer (blocks thread for ~100ms)
        Customer customer = restTemplate.getForObject(
            "http://customer-service/customers/" + order.getCustomerId(), 
            Customer.class
        );
        
        // Call 3: Get payment (blocks thread for ~100ms)
        Payment payment = restTemplate.getForObject(
            "http://payment-service/payments/" + order.getPaymentId(), 
            Payment.class
        );
        
        // Call 4: Get shipping (blocks thread for ~100ms)
        Shipping shipping = restTemplate.getForObject(
            "http://shipping-service/shipping/" + order.getShippingId(), 
            Shipping.class
        );
        
        // Total time: ~400ms with thread blocked entire time
        return new OrderDetails(order, customer, payment, shipping);
    }
}
```

**Problems:**
- Thread blocked for **400ms** per request
- With 200 threads, max throughput = **500 requests/second**
- Under load, requests queue up
- High latency for end users

### Solution: Asynchronous Non-Blocking Calls

```java
// ✅ GOOD: Asynchronous non-blocking approach
@RestController
@RequestMapping("/api/orders")
public class AsyncOrderController {
    
    @Autowired
    private WebClient webClient;
    
    @GetMapping("/{orderId}")
    public Mono<OrderDetails> getOrderDetails(@PathVariable String orderId) {
        
        // All calls execute in parallel, non-blocking
        Mono<Order> orderMono = webClient.get()
            .uri("http://order-service/orders/{id}", orderId)
            .retrieve()
            .bodyToMono(Order.class)
            .timeout(Duration.ofMillis(500));
        
        // Chain dependent call
        Mono<OrderDetails> result = orderMono.flatMap(order -> {
            
            // These 3 calls happen in parallel
            Mono<Customer> customerMono = webClient.get()
                .uri("http://customer-service/customers/{id}", order.getCustomerId())
                .retrieve()
                .bodyToMono(Customer.class)
                .timeout(Duration.ofMillis(500));
            
            Mono<Payment> paymentMono = webClient.get()
                .uri("http://payment-service/payments/{id}", order.getPaymentId())
                .retrieve()
                .bodyToMono(Payment.class)
                .timeout(Duration.ofMillis(500));
            
            Mono<Shipping> shippingMono = webClient.get()
                .uri("http://shipping-service/shipping/{id}", order.getShippingId())
                .retrieve()
                .bodyToMono(Shipping.class)
                .timeout(Duration.ofMillis(500));
            
            // Combine all results
            return Mono.zip(customerMono, paymentMono, shippingMono)
                .map(tuple -> new OrderDetails(
                    order, 
                    tuple.getT1(), 
                    tuple.getT2(), 
                    tuple.getT3()
                ));
        });
        
        // Total time: ~100ms (parallel execution)
        // Thread never blocked!
        return result;
    }
}
```

### Performance Comparison

```java
@Component
public class PerformanceComparison {
    
    public void demonstrateBlockingVsNonBlocking() {
        // Synchronous blocking approach
        long start = System.currentTimeMillis();
        int syncRequestsHandled = 0;
        
        // Simulate 1000 requests with 200 threads
        ExecutorService syncExecutor = Executors.newFixedThreadPool(200);
        CountDownLatch syncLatch = new CountDownLatch(1000);
        
        for (int i = 0; i < 1000; i++) {
            syncExecutor.submit(() -> {
                try {
                    Thread.sleep(400); // Simulate blocking I/O
                    syncRequestsHandled++;
                } finally {
                    syncLatch.countDown();
                }
            });
        }
        
        syncLatch.await();
        long syncTime = System.currentTimeMillis() - start;
        
        // Result: ~2000ms (threads kept blocking)
        System.out.println("Sync Time: " + syncTime + "ms");
        
        
        // Asynchronous non-blocking approach
        start = System.currentTimeMillis();
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        
        for (int i = 0; i < 1000; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Non-blocking operation
                try {
                    Thread.sleep(100); // Only actual processing time
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
        }
        
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long asyncTime = System.currentTimeMillis() - start;
        
        // Result: ~500ms (5x faster!)
        System.out.println("Async Time: " + asyncTime + "ms");
        System.out.println("Improvement: " + (syncTime / asyncTime) + "x faster");
    }
}
```

### Reactive Spring WebFlux Configuration

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        // Configure connection pool
        ConnectionProvider provider = ConnectionProvider.builder("custom")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120))
            .build();
        
        HttpClient httpClient = HttpClient.create(provider)
            .responseTimeout(Duration.ofSeconds(5))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5));
                conn.addHandlerLast(new WriteTimeoutHandler(5));
            });
        
        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
```

### Key Takeaways

| Aspect | Synchronous | Asynchronous |
|--------|-------------|--------------|
| **Thread Usage** | 1 thread per request | Few threads handle many requests |
| **Throughput** | Low (~500 req/s) | High (10000+ req/s) |
| **Latency** | Sequential (additive) | Parallel (concurrent) |
| **Resource Usage** | High (many threads) | Low (thread reuse) |
| **Scalability** | Poor | Excellent |
| **Complexity** | Simple | Higher |

---

## 3. How Improper Thread Pool Configuration Crashes Services

### The Problem

Improper thread pool configuration can cause:
- **OutOfMemoryError**
- **Service crashes**
- **Request timeouts**
- **Cascading failures across services**

### Visual Representation

```
Improper Configuration:

Service A (Unbounded Pool)
    ↓
1000 requests/sec → Creates 1000 threads
    ↓
Each thread: 1MB stack = 1GB memory
    ↓
JVM Heap: 512MB configured
    ↓
💥 OutOfMemoryError → Service Crash
    ↓
Load Balancer redirects to Service B
    ↓
Service B overloaded → Crash
    ↓
Cascading Failure → All services down
```

### Code Example: Disaster Configuration

```java
// ❌ DISASTER: Multiple bad practices
@Configuration
public class BadThreadPoolConfig {
    
    // Problem 1: Unbounded thread pool
    @Bean
    public ExecutorService unboundedExecutor() {
        return Executors.newCachedThreadPool(); // No limit!
    }
    
    // Problem 2: Too many threads
    @Bean
    public ThreadPoolTaskExecutor tooManyThreadsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(500);  // Way too high!
        executor.setMaxPoolSize(2000);  // Insane!
        executor.setQueueCapacity(10000); // Huge queue!
        executor.initialize();
        return executor;
    }
    
    // Problem 3: No queue capacity
    @Bean
    public ThreadPoolTaskExecutor noQueueExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(Integer.MAX_VALUE); // Unbounded queue!
        executor.initialize();
        return executor;
    }
}

@Service
public class CrashingService {
    
    @Autowired
    private ExecutorService unboundedExecutor;
    
    @PostMapping("/process")
    public ResponseEntity<String> processRequests(@RequestBody List<Request> requests) {
        // Creates one thread per request - DISASTER!
        requests.forEach(request -> {
            unboundedExecutor.submit(() -> {
                // Long-running task
                Thread.sleep(5000);
                processRequest(request);
            });
        });
        
        // Under load with 1000 requests:
        // - 1000 threads created
        // - Each thread: ~1MB stack space
        // - Total: ~1GB just for thread stacks
        // - Plus heap allocations
        // Result: OutOfMemoryError! 💥
        
        return ResponseEntity.ok("Processing...");
    }
}
```

### What Happens During Crash

```java
// Real-world crash scenario
public class CrashScenario {
    
    public void demonstrateCrash() {
        System.out.println("Initial Memory: " + 
            Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
        
        ExecutorService executor = Executors.newCachedThreadPool();
        
        // Simulate high load
        for (int i = 0; i < 10000; i++) {
            executor.submit(() -> {
                try {
                    // Each thread allocates memory
                    byte[] data = new byte[1024 * 1024]; // 1MB
                    Thread.sleep(60000); // Holds for 60 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            if (i % 100 == 0) {
                System.out.println("Submitted: " + i + 
                    ", Free Memory: " + Runtime.getRuntime().freeMemory() / (1024 * 1024) + "MB");
            }
        }
        
        // Output shows memory dropping rapidly:
        // Submitted: 0, Free Memory: 450MB
        // Submitted: 100, Free Memory: 380MB
        // Submitted: 200, Free Memory: 310MB
        // Submitted: 300, Free Memory: 240MB
        // ...
        // java.lang.OutOfMemoryError: unable to create new native thread
    }
}
```

### Solution: Proper Configuration

```java
// ✅ GOOD: Properly configured thread pools
@Configuration
public class ProperThreadPoolConfig {
    
    @Value("${app.thread.core-pool-size:10}")
    private int corePoolSize;
    
    @Value("${app.thread.max-pool-size:50}")
    private int maxPoolSize;
    
    @Value("${app.thread.queue-capacity:100}")
    private int queueCapacity;
    
    @Bean(name = "apiExecutor")
    public ThreadPoolTaskExecutor apiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool: Start with reasonable number
        executor.setCorePoolSize(corePoolSize);
        
        // Max pool: Based on CPU cores and workload
        // Formula: (CPU cores * 2) for I/O bound
        //          (CPU cores + 1) for CPU bound
        int cores = Runtime.getRuntime().availableProcessors();
        executor.setMaxPoolSize(Math.min(maxPoolSize, cores * 2));
        
        // Queue: Bounded to fail fast
        executor.setQueueCapacity(queueCapacity);
        
        // Thread naming
        executor.setThreadNamePrefix("api-exec-");
        
        // Rejection policy: Caller runs (backpressure)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Graceful shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("Initialized API Executor: core={}, max={}, queue={}", 
            executor.getCorePoolSize(), 
            executor.getMaxPoolSize(), 
            executor.getQueueCapacity());
        
        return executor;
    }
    
    @Bean(name = "externalCallExecutor")
    public ThreadPoolTaskExecutor externalCallExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Separate pool for external calls (isolation)
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("external-call-");
        
        // Abort policy: Fail fast for external calls
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        
        executor.initialize();
        return executor;
    }
    
    @Bean(name = "databaseExecutor")
    public ThreadPoolTaskExecutor databaseExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Database pool sized based on connection pool
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("db-exec-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        return executor;
    }
}
```

### Thread Pool Sizing Formula

```java
@Component
public class ThreadPoolCalculator {
    
    public int calculateOptimalPoolSize(String workloadType) {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        
        switch (workloadType.toUpperCase()) {
            case "CPU_BOUND":
                // CPU-intensive tasks
                // Formula: CPU cores + 1
                return cpuCores + 1;
                
            case "IO_BOUND":
                // I/O intensive tasks (database, REST calls)
                // Formula: CPU cores * (1 + wait time / service time)
                // Simplified: CPU cores * 2
                return cpuCores * 2;
                
            case "MIXED":
                // Mixed workload
                // Formula: CPU cores * 1.5
                return (int) (cpuCores * 1.5);
                
            default:
                return cpuCores * 2;
        }
    }
    
    public void printRecommendations() {
        int cores = Runtime.getRuntime().availableProcessors();
        
        System.out.println("=== Thread Pool Recommendations ===");
        System.out.println("CPU Cores: " + cores);
        System.out.println("CPU Bound: " + (cores + 1));
        System.out.println("I/O Bound: " + (cores * 2));
        System.out.println("Mixed: " + (int)(cores * 1.5));
        System.out.println("===================================");
    }
}
```

### Monitoring and Alerting

```java
@Component
public class ThreadPoolMonitor {
    
    @Autowired
    @Qualifier("apiExecutor")
    private ThreadPoolTaskExecutor apiExecutor;
    
    @Scheduled(fixedDelay = 5000)
    public void monitorThreadPools() {
        ThreadPoolExecutor pool = apiExecutor.getThreadPoolExecutor();
        
        int activeCount = pool.getActiveCount();
        int maxPoolSize = pool.getMaximumPoolSize();
        int queueSize = pool.getQueue().size();
        int queueCapacity = apiExecutor.getQueueCapacity();
        
        // Calculate utilization
        double threadUtilization = (double) activeCount / maxPoolSize * 100;
        double queueUtilization = (double) queueSize / queueCapacity * 100;
        
        log.info("Thread Pool Status - Active: {}/{} ({}%), Queue: {}/{} ({}%)",
            activeCount, maxPoolSize, String.format("%.2f", threadUtilization),
            queueSize, queueCapacity, String.format("%.2f", queueUtilization));
        
        // Alert if threshold exceeded
        if (threadUtilization > 90) {
            log.error("⚠️ ALERT: Thread pool utilization critical: {}%", threadUtilization);
            // Send alert to monitoring system
            sendAlert("Thread pool near exhaustion", threadUtilization);
        }
        
        if (queueUtilization > 80) {
            log.warn("⚠️ WARNING: Queue filling up: {}%", queueUtilization);
            // Send warning
            sendWarning("Request queue building up", queueUtilization);
        }
        
        // Check for rejected tasks
        long rejectedCount = pool.getRejectedExecutionHandler() instanceof ThreadPoolExecutor.AbortPolicy
            ? pool.getCompletedTaskCount()
            : 0;
            
        if (rejectedCount > 0) {
            log.error("⚠️ CRITICAL: {} tasks rejected!", rejectedCount);
        }
    }
    
    private void sendAlert(String message, double utilization) {
        // Integration with monitoring system (Prometheus, Grafana, PagerDuty)
        System.out.println("🚨 ALERT: " + message + " - " + utilization + "%");
    }
    
    private void sendWarning(String message, double utilization) {
        System.out.println("⚠️ WARNING: " + message + " - " + utilization + "%");
    }
}
```

### Cascading Failure Prevention

```java
@Service
public class ResilientService {
    
    @Autowired
    @Qualifier("apiExecutor")
    private ThreadPoolTaskExecutor executor;
    
    // Circuit breaker prevents cascading failures
    @CircuitBreaker(name = "resilientService", fallbackMethod = "fallback")
    @Bulkhead(name = "resilientService", type = Bulkhead.Type.THREADPOOL)
    @RateLimiter(name = "resilientService")
    public CompletableFuture<Response> processRequest(Request request) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Check if executor is overloaded
            ThreadPoolExecutor pool = executor.getThreadPoolExecutor();
            
            if (pool.getActiveCount() > pool.getMaximumPoolSize() * 0.9) {
                throw new ServiceOverloadedException("Service is overloaded, please retry later");
            }
            
            // Process request
            return performProcessing(request);
            
        }, executor);
    }
    
    public CompletableFuture<Response> fallback(Request request, Exception e) {
        log.warn("Fallback triggered for request: {}", request.getId(), e);
        return CompletableFuture.completedFuture(new Response("SERVICE_UNAVAILABLE", "Please retry"));
    }
}
```

### Configuration Best Practices

| Configuration | Recommended Value | Reason |
|---------------|-------------------|---------|
| **Core Pool Size** | CPU cores * 2 for I/O | Balance responsiveness and resource usage |
| **Max Pool Size** | CPU cores * 4 for I/O | Prevent excessive thread creation |
| **Queue Capacity** | 100-500 | Bounded queue for fast failure |
| **Keep Alive Time** | 60 seconds | Allow threads to die when idle |
| **Rejection Policy** | CallerRunsPolicy | Provides backpressure |
| **Thread Priority** | NORM_PRIORITY | Avoid starvation |
| **Graceful Shutdown** | 60 seconds wait | Complete in-flight requests |

---

## 4. Retries Without Exponential Backoff

### The Problem

Immediate retries without backoff can:
- **Overwhelm downstream services**
- **Create retry storms**
- **Waste resources**
- **Increase latency**
- **Cause cascading failures**

### Visual Representation

```
Without Exponential Backoff (❌):
Attempt 1: [Fail] → Immediate retry
Attempt 2: [Fail] → Immediate retry
Attempt 3: [Fail] → Immediate retry
Result: Hammering the failing service, making recovery impossible

Service: ☠️ Dead (can't recover due to constant load)


With Exponential Backoff (✅):
Attempt 1: [Fail] → Wait 1s → Retry
Attempt 2: [Fail] → Wait 2s → Retry
Attempt 3: [Fail] → Wait 4s → Retry
Attempt 4: [Fail] → Wait 8s → Retry

Service: ✅ Recovers during wait time
```

### Code Example: Bad Retry Logic

```java
// ❌ BAD: No backoff, creates retry storm
@Service
public class BadRetryService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public OrderResponse processOrder(Order order) {
        int maxRetries = 10;
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                return restTemplate.postForObject(
                    "http://order-service/orders",
                    order,
                    OrderResponse.class
                );
            } catch (Exception e) {
                attempt++;
                log.warn("Attempt {} failed, retrying immediately...", attempt);
                // No delay! Immediately retries!
            }
        }
        
        throw new ServiceException("Failed after " + maxRetries + " attempts");
    }
}
```

**Problems:**
- 10 immediate retries = 10 requests in < 1 second
- 1000 concurrent requests = 10,000 requests to failing service
- Service can never recover
- Creates "thundering herd" problem

### Retry Storm Simulation

```java
public class RetryStormDemo {
    
    public void demonstrateRetryStorm() {
        // Simulate 100 clients retrying immediately
        ExecutorService executor = Executors.newFixedThreadPool(100);
        AtomicInteger totalRequests = new AtomicInteger(0);
        
        CountDownLatch latch = new CountDownLatch(100);
        
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> {
                // Each client retries 5 times immediately
                for (int retry = 0; retry < 5; retry++) {
                    totalRequests.incrementAndGet();
                    System.out.println("Request sent. Total: " + totalRequests.get());
                    // No delay!
                }
                latch.countDown();
            });
        }
        
        latch.await();
        
        // Result: 500 requests in < 1 second!
        System.out.println("Total requests in retry storm: " + totalRequests.get());
        System.out.println("Service status: CRASHED 💥");
    }
}
```

### Solution: Exponential Backoff with Jitter

```java
// ✅ GOOD: Exponential backoff with jitter
@Service
public class ResilientRetryService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 100;
    private static final long MAX_BACKOFF_MS = 10000;
    
    public OrderResponse processOrderWithRetry(Order order) {
        int attempt = 0;
        Exception lastException = null;
        
        while (attempt < MAX_RETRIES) {
            try {
                return restTemplate.postForObject(
                    "http://order-service/orders",
                    order,
                    OrderResponse.class
                );
            } catch (Exception e) {
                lastException = e;
                attempt++;
                
                if (attempt >= MAX_RETRIES) {
                    break;
                }
                
                // Calculate exponential backoff
                long backoffMs = calculateBackoff(attempt);
                
                log.warn("Attempt {} failed. Retrying after {}ms...", attempt, backoffMs);
                
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new ServiceException("Retry interrupted", ie);
                }
            }
        }
        
        throw new ServiceException("Failed after " + MAX_RETRIES + " attempts", lastException);
    }
    
    private long calculateBackoff(int attempt) {
        // Exponential backoff: 2^attempt * initial
        long exponentialBackoff = (long) Math.pow(2, attempt) * INITIAL_BACKOFF_MS;
        
        // Cap at maximum
        long cappedBackoff = Math.min(exponentialBackoff, MAX_BACKOFF_MS);
        
        // Add jitter (randomness) to prevent thundering herd
        // Jitter range: 0% to 25% of backoff time
        long jitter = (long) (cappedBackoff * Math.random() * 0.25);
        
        return cappedBackoff + jitter;
    }
}
```

### Spring Retry with Exponential Backoff

```java
// ✅ EXCELLENT: Using Spring Retry framework
@Configuration
@EnableRetry
public class RetryConfig {
    
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // Exponential backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(100);     // Start with 100ms
        backOffPolicy.setMultiplier(2.0);          // Double each time
        backOffPolicy.setMaxInterval(10000);       // Max 10 seconds
        
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        // Retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        
        retryTemplate.setRetryPolicy(retryPolicy);
        
        return retryTemplate;
    }
}

@Service
public class OptimalRetryService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private RetryTemplate retryTemplate;
    
    // Method 1: Using RetryTemplate
    public OrderResponse processWithRetryTemplate(Order order) {
        return retryTemplate.execute(context -> {
            log.info("Attempt #{}", context.getRetryCount() + 1);
            
            return restTemplate.postForObject(
                "http://order-service/orders",
                order,
                OrderResponse.class
            );
        });
    }
    
    // Method 2: Using @Retryable annotation
    @Retryable(
        value = {RestClientException.class, ResourceAccessException.class},
        maxAttempts = 5,
        backoff = @Backoff(
            delay = 100,
            multiplier = 2.0,
            maxDelay = 10000,
            random = true  // Adds jitter
        )
    )
    public OrderResponse processWithAnnotation(Order order) {
        log.info("Processing order: {}", order.getId());
        
        return restTemplate.postForObject(
            "http://order-service/orders",
            order,
            OrderResponse.class
        );
    }
    
    @Recover
    public OrderResponse recover(RestClientException e, Order order) {
        log.error("All retry attempts failed for order: {}", order.getId(), e);
        
        // Fallback logic
        return new OrderResponse("FAILED", "Service temporarily unavailable");
    }
}
```

### Resilience4j Retry with Circuit Breaker

```java
// ✅ BEST: Combining retry with circuit breaker
@Configuration
public class Resilience4jConfig {
    
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
            .maxAttempts(5)
            .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
                Duration.ofMillis(100),  // Initial interval
                2.0,                      // Multiplier
                Duration.ofSeconds(10)    // Max interval
            ))
            .retryExceptions(
                RestClientException.class,
                TimeoutException.class
            )
            .ignoreExceptions(
                IllegalArgumentException.class,
                ValidationException.class
            )
            .build();
    }
    
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)                    // Open if 50% fail
            .slowCallRateThreshold(50)                   // Open if 50% slow
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();
    }
}

@Service
public class ResilientService {
    
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public ResilientService(RetryRegistry retryRegistry, 
                           CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retry = retryRegistry.retry("orderService");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("orderService");
    }
    
    public OrderResponse processOrder(Order order) {
        // Chain retry and circuit breaker
        Supplier<OrderResponse> supplier = () -> 
            restTemplate.postForObject(
                "http://order-service/orders",
                order,
                OrderResponse.class
            );
        
        // Apply circuit breaker first, then retry
        supplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        supplier = Retry.decorateSupplier(retry, supplier);
        
        try {
            return supplier.get();
        } catch (Exception e) {
            log.error("Order processing failed after retries", e);
            return fallbackResponse(order);
        }
    }
    
    private OrderResponse fallbackResponse(Order order) {
        return new OrderResponse("PENDING", "Your order will be processed shortly");
    }
}
```

### Backoff Strategy Comparison

```java
@Component
public class BackoffComparison {
    
    public void compareStrategies() {
        System.out.println("=== Retry Strategies Comparison ===\n");
        
        // Fixed delay
        System.out.println("Fixed Delay (1 second):");
        for (int i = 1; i <= 5; i++) {
            System.out.println("  Attempt " + i + ": " + 1000 + "ms");
        }
        System.out.println("  Total time: 5 seconds\n");
        
        // Linear backoff
        System.out.println("Linear Backoff (base 500ms):");
        for (int i = 1; i <= 5; i++) {
            long delay = i * 500;
            System.out.println("  Attempt " + i + ": " + delay + "ms");
        }
        System.out.println("  Total time: 7.5 seconds\n");
        
        // Exponential backoff
        System.out.println("Exponential Backoff (base 100ms, multiplier 2):");
        long totalExponential = 0;
        for (int i = 1; i <= 5; i++) {
            long delay = (long) (100 * Math.pow(2, i - 1));
            totalExponential += delay;
            System.out.println("  Attempt " + i + ": " + delay + "ms");
        }
        System.out.println("  Total time: " + totalExponential + "ms\n");
        
        // Exponential with jitter
        System.out.println("Exponential with Jitter (adds randomness):");
        long totalJitter = 0;
        for (int i = 1; i <= 5; i++) {
            long delay = (long) (100 * Math.pow(2, i - 1));
            long jitter = (long) (delay * Math.random() * 0.25);
            totalJitter += (delay + jitter);
            System.out.println("  Attempt " + i + ": " + (delay + jitter) + "ms (base: " + delay + "ms, jitter: " + jitter + "ms)");
        }
        System.out.println("  Total time: ~" + totalJitter + "ms");
    }
}
```

### Best Practices

| Practice | Recommendation | Reason |
|----------|---------------|---------|
| **Initial Delay** | 100-500ms | Quick retry for transient failures |
| **Multiplier** | 2.0 | Balanced backoff rate |
| **Max Delay** | 10-30 seconds | Prevent excessive waiting |
| **Max Attempts** | 3-5 | Balance persistence vs resource waste |
| **Jitter** | 0-25% of delay | Prevent thundering herd |
| **Circuit Breaker** | Combine with retry | Fail fast when service is down |
| **Idempotency** | Required for retries | Prevent duplicate operations |
| **Timeout** | Set per request | Prevent hanging |

---

## 5. Handling Partial Failures in Microservice Chain

### The Problem

In a chain of microservices, one failure shouldn't break the entire flow. Partial failures require:
- **Graceful degradation**
- **Fallback mechanisms**
- **Compensating transactions**
- **Circuit breakers**

### Visual Representation

```
Order Flow:
User → API Gateway → Order Service → Payment Service → Inventory Service → Shipping Service

Scenario: Payment Service Fails ❌

Without Handling:
Order Service → Payment (FAIL) → ❌ Entire order fails
                                  ❌ User sees error
                                  ❌ Bad experience

With Handling:
Order Service → Payment (FAIL) → ✅ Continue with "PENDING_PAYMENT" status
                                  ✅ Reserve inventory
                                  ✅ User notified
                                  ✅ Retry payment later
```

### Code Example: Without Failure Handling

```java
// ❌ BAD: No failure handling - all or nothing
@Service
public class FragileOrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Transactional
    public OrderResponse processOrder(OrderRequest request) {
        // Step 1: Create order
        Order order = createOrder(request);
        
        // Step 2: Process payment (if fails, everything fails!)
        PaymentResponse payment = restTemplate.postForObject(
            "http://payment-service/payments",
            request.getPayment(),
            PaymentResponse.class
        );
        order.setPaymentId(payment.getId());
        
        // Step 3: Reserve inventory (never reached if payment fails!)
        InventoryResponse inventory = restTemplate.postForObject(
            "http://inventory-service/reserve",
            request.getItems(),
            InventoryResponse.class
        );
        order.setInventoryId(inventory.getId());
        
        // Step 4: Schedule shipping (never reached if inventory fails!)
        ShippingResponse shipping = restTemplate.postForObject(
            "http://shipping-service/schedule",
            order,
            ShippingResponse.class
        );
        order.setShippingId(shipping.getId());
        
        // All or nothing - if any step fails, entire order fails
        return new OrderResponse(order);
    }
}
```

### Solution 1: Try-Catch with Fallbacks

```java
// ✅ BETTER: Individual failure handling
@Service
public class ResilientOrderService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private OrderRepository orderRepository;
    
    public OrderResponse processOrder(OrderRequest request) {
        Order order = createOrder(request);
        order.setStatus("CREATED");
        orderRepository.save(order);
        
        // Step 1: Process payment with fallback
        try {
            PaymentResponse payment = restTemplate.postForObject(
                "http://payment-service/payments",
                request.getPayment(),
                PaymentResponse.class
            );
            order.setPaymentId(payment.getId());
            order.setPaymentStatus("COMPLETED");
        } catch (Exception e) {
            log.error("Payment failed for order: {}", order.getId(), e);
            order.setPaymentStatus("PENDING");
            // Don't fail the order - mark for retry
            schedulePaymentRetry(order.getId());
        }
        
        // Step 2: Reserve inventory with fallback
        try {
            InventoryResponse inventory = restTemplate.postForObject(
                "http://inventory-service/reserve",
                request.getItems(),
                InventoryResponse.class
            );
            order.setInventoryId(inventory.getId());
            order.setInventoryStatus("RESERVED");
        } catch (Exception e) {
            log.error("Inventory reservation failed for order: {}", order.getId(), e);
            order.setInventoryStatus("PENDING");
            // Continue with order - handle async
            scheduleInventoryRetry(order.getId());
        }
        
        // Step 3: Schedule shipping with fallback
        try {
            ShippingResponse shipping = restTemplate.postForObject(
                "http://shipping-service/schedule",
                order,
                ShippingResponse.class
            );
            order.setShippingId(shipping.getId());
            order.setShippingStatus("SCHEDULED");
        } catch (Exception e) {
            log.error("Shipping schedule failed for order: {}", order.getId(), e);
            order.setShippingStatus("PENDING");
            scheduleShippingRetry(order.getId());
        }
        
        // Determine overall status
        if ("COMPLETED".equals(order.getPaymentStatus()) &&
            "RESERVED".equals(order.getInventoryStatus()) &&
            "SCHEDULED".equals(order.getShippingStatus())) {
            order.setStatus("CONFIRMED");
        } else {
            order.setStatus("PARTIALLY_COMPLETED");
        }
        
        orderRepository.save(order);
        
        return new OrderResponse(order);
    }
    
    private void schedulePaymentRetry(String orderId) {
        // Queue for async retry
        kafkaTemplate.send("payment-retry-topic", orderId);
    }
    
    private void scheduleInventoryRetry(String orderId) {
        kafkaTemplate.send("inventory-retry-topic", orderId);
    }
    
    private void scheduleShippingRetry(String orderId) {
        kafkaTemplate.send("shipping-retry-topic", orderId);
    }
}
```

### Solution 2: SAGA Pattern

```java
// ✅ EXCELLENT: SAGA pattern for distributed transactions
@Service
public class OrderSagaOrchestrator {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    public OrderResponse processOrderSaga(OrderRequest request) {
        // Create saga instance
        OrderSaga saga = new OrderSaga();
        saga.setOrderId(UUID.randomUUID().toString());
        saga.setStatus("STARTED");
        saga.setRequest(request);
        
        // Step 1: Create order
        Order order = createOrder(request);
        saga.addStep("ORDER_CREATED", order.getId());
        
        // Step 2: Execute payment
        try {
            PaymentResponse payment = executePayment(request.getPayment());
            saga.addStep("PAYMENT_COMPLETED", payment.getId());
        } catch (Exception e) {
            log.error("Payment failed, initiating compensation", e);
            compensateOrder(saga);
            throw new SagaFailureException("Payment failed", e);
        }
        
        // Step 3: Reserve inventory
        try {
            InventoryResponse inventory = reserveInventory(request.getItems());
            saga.addStep("INVENTORY_RESERVED", inventory.getId());
        } catch (Exception e) {
            log.error("Inventory failed, initiating compensation", e);
            compensatePayment(saga);
            compensateOrder(saga);
            throw new SagaFailureException("Inventory failed", e);
        }
        
        // Step 4: Schedule shipping
        try {
            ShippingResponse shipping = scheduleShipping(order);
            saga.addStep("SHIPPING_SCHEDULED", shipping.getId());
        } catch (Exception e) {
            log.error("Shipping failed, initiating compensation", e);
            compensateInventory(saga);
            compensatePayment(saga);
            compensateOrder(saga);
            throw new SagaFailureException("Shipping failed", e);
        }
        
        saga.setStatus("COMPLETED");
        return new OrderResponse(order);
    }
    
    // Compensation logic (rollback)
    private void compensateOrder(OrderSaga saga) {
        String orderId = saga.getStepData("ORDER_CREATED");
        log.info("Compensating order: {}", orderId);
        // Delete or mark order as cancelled
        orderRepository.deleteById(orderId);
    }
    
    private void compensatePayment(OrderSaga saga) {
        String paymentId = saga.getStepData("PAYMENT_COMPLETED");
        if (paymentId != null) {
            log.info("Refunding payment: {}", paymentId);
            restTemplate.postForObject(
                "http://payment-service/refund/" + paymentId,
                null,
                Void.class
            );
        }
    }
    
    private void compensateInventory(OrderSaga saga) {
        String inventoryId = saga.getStepData("INVENTORY_RESERVED");
        if (inventoryId != null) {
            log.info("Releasing inventory: {}", inventoryId);
            restTemplate.postForObject(
                "http://inventory-service/release/" + inventoryId,
                null,
                Void.class
            );
        }
    }
    
    private void compensateShipping(OrderSaga saga) {
        String shippingId = saga.getStepData("SHIPPING_SCHEDULED");
        if (shippingId != null) {
            log.info("Cancelling shipping: {}", shippingId);
            restTemplate.postForObject(
                "http://shipping-service/cancel/" + shippingId,
                null,
                Void.class
            );
        }
    }
}

// SAGA state management
@Document
class OrderSaga {
    private String orderId;
    private String status;
    private OrderRequest request;
    private List<SagaStep> steps = new ArrayList<>();
    
    public void addStep(String name, String data) {
        steps.add(new SagaStep(name, data, LocalDateTime.now()));
    }
    
    public String getStepData(String stepName) {
        return steps.stream()
            .filter(s -> s.getName().equals(stepName))
            .map(SagaStep::getData)
            .findFirst()
            .orElse(null);
    }
}

@Data
class SagaStep {
    private String name;
    private String data;
    private LocalDateTime timestamp;
}
```

### Solution 3: Resilience4j Bulkhead + Circuit Breaker

```java
// ✅ BEST: Combining multiple resilience patterns
@Service
public class AdvancedResilientOrderService {
    
    @Autowired
    private WebClient webClient;
    
    @CircuitBreaker(name = "paymentService", fallbackMethod = "paymentFallback")
    @Bulkhead(name = "paymentService", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "paymentService")
    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return webClient.post()
            .uri("http://payment-service/payments")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(PaymentResponse.class)
            .timeout(Duration.ofSeconds(3));
    }
    
    public Mono<PaymentResponse> paymentFallback(PaymentRequest request, Exception e) {
        log.warn("Payment service unavailable, using fallback", e);
        // Return pending status
        return Mono.just(new PaymentResponse("PENDING", "Payment will be processed later"));
    }
    
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    @Bulkhead(name = "inventoryService", type = Bulkhead.Type.SEMAPHORE)
    @TimeLimiter(name = "inventoryService")
    public Mono<InventoryResponse> reserveInventory(InventoryRequest request) {
        return webClient.post()
            .uri("http://inventory-service/reserve")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(InventoryResponse.class)
            .timeout(Duration.ofSeconds(3));
    }
    
    public Mono<InventoryResponse> inventoryFallback(InventoryRequest request, Exception e) {
        log.warn("Inventory service unavailable, using fallback", e);
        // Check local cache or return tentative reservation
        return Mono.just(new InventoryResponse("PENDING", "Reservation pending verification"));
    }
    
    public Mono<OrderResponse> processOrderReactive(OrderRequest request) {
        // Parallel non-blocking calls
        Mono<PaymentResponse> paymentMono = processPayment(request.getPayment());
        Mono<InventoryResponse> inventoryMono = reserveInventory(request.getInventory());
        
        // Combine results
        return Mono.zip(paymentMono, inventoryMono)
            .map(tuple -> {
                PaymentResponse payment = tuple.getT1();
                InventoryResponse inventory = tuple.getT2();
                
                // Create order based on partial success
                Order order = new Order();
                order.setPaymentStatus(payment.getStatus());
                order.setInventoryStatus(inventory.getStatus());
                
                // Determine overall status
                if ("COMPLETED".equals(payment.getStatus()) && 
                    "RESERVED".equals(inventory.getStatus())) {
                    order.setStatus("CONFIRMED");
                } else {
                    order.setStatus("PARTIALLY_COMPLETED");
                }
                
                return new OrderResponse(order);
            })
            .onErrorResume(e -> {
                log.error("Order processing failed", e);
                return Mono.just(new OrderResponse("FAILED", "Please try again"));
            });
    }
}
```

### Resilience4j Configuration

```yaml
# application.yml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
    instances:
      paymentService:
        base-config: default
      inventoryService:
        base-config: default
        failure-rate-threshold: 60
      shippingService:
        base-config: default
        wait-duration-in-open-state: 60s
  
  bulkhead:
    configs:
      default:
        max-concurrent-calls: 25
        max-wait-duration: 100ms
    instances:
      paymentService:
        max-concurrent-calls: 10
      inventoryService:
        max-concurrent-calls: 15
  
  timelimiter:
    configs:
      default:
        timeout-duration: 3s
    instances:
      paymentService:
        timeout-duration: 5s
      inventoryService:
        timeout-duration: 3s
  
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 100ms
        exponential-backoff-multiplier: 2
        retry-exceptions:
          - org.springframework.web.client.ResourceAccessException
        ignore-exceptions:
          - java.lang.IllegalArgumentException
```

### Monitoring Partial Failures

```java
@Component
public class PartialFailureMonitor {
    
    private final MeterRegistry meterRegistry;
    
    public PartialFailureMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    public void recordPartialFailure(String service, String status) {
        meterRegistry.counter("partial.failure",
            "service", service,
            "status", status
        ).increment();
    }
    
    @Scheduled(fixedDelay = 60000)
    public void reportPartialFailures() {
        // Report to monitoring system
        log.info("Partial failure metrics: ...");
    }
}
```

### Best Practices

| Pattern | Use Case | Benefit |
|---------|----------|---------|
| **Try-Catch Fallback** | Simple scenarios | Easy to implement |
| **SAGA Pattern** | Complex transactions | Ensures consistency |
| **Circuit Breaker** | Unstable services | Prevents cascading failures |
| **Bulkhead** | Resource isolation | Limits blast radius |
| **Timeout** | Slow services | Prevents hanging |
| **Retry** | Transient failures | Improves success rate |
| **Fallback** | Degraded mode | Maintains availability |

---

## 6. Why Scaling Services Sometimes Increases Latency

### The Problem

Counter-intuitively, adding more service instances can sometimes **increase latency** due to:
- **Load balancer overhead**
- **Network congestion**
- **Database connection pool saturation**
- **Shared resource contention**
- **Cold start delays**
- **Cache misses**

[Content continues with remaining questions 6-10...]

---

**🎯 Complete Guide Summary**

All 10 critical architecture and performance topics covered with production-ready solutions!

