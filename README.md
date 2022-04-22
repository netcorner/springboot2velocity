# springboot2 集成 velocity2.3
## 产生背景：springboot2居然抛弃 velocity 了，自己整合一个吧 
 
 


 ## Step 1. Add the JitPack repository to your build file
    <repositories>
        <repository>
          <id>jitpack.io</id>
          <url>https://jitpack.io</url>
        </repository>
    </repositories>
    

## Step 2. Add the dependency
    <dependency>
        <groupId>com.github.netcorner</groupId>
        <artifactId>springboot2velocity</artifactId>
        <version>1.0</version>
    </dependency>

## Step 3. Configuration
       @Configuration
       public class VelocityConfig {
           @Value("${spring.velocity.prefix:/templates/}")
           private String prefix;
       
           @Value("${spring.velocity.suffix:.html}")
           private String suffix;
       
       
           @Bean
           public VelocityConfigurer getVelocityConfigurer()
           {
       
       
               VelocityConfigurer velocityConfigurer=new VelocityConfigurer();
               velocityConfigurer.setResourceLoaderPath(prefix);
               Properties properties=new Properties();
               properties.put("velocimacro.permissions.allow.inline.local.scope",true);
               properties.put("input.encoding","UTF-8");
               properties.put("output.encoding","UTF-8");
               velocityConfigurer.setVelocityProperties(properties);
               return velocityConfigurer;
           }
       
           @Bean
           public ViewResolver getVelocityViewResolver()
           {
       
               VelocityViewResolver velocityViewResolver=new VelocityViewResolver();
               velocityViewResolver.setCache(false);
               velocityViewResolver.setPrefix(prefix);
               velocityViewResolver.setSuffix(suffix);
               velocityViewResolver.setContentType("text/html;charset=UTF-8");
               velocityViewResolver.setExposeSpringMacroHelpers(true);
               velocityViewResolver.setExposeRequestAttributes(true);
               velocityViewResolver.setRequestContextAttribute("request");
               //velocityViewResolver.setToolboxConfigLocation("/velocity-toolbox.xml");
               //velocityViewResolver.setViewClass(VelocityToolbox20View.class);
       
       //        Properties properties=new Properties();
       //        velocityViewResolver.setVelocityProperties(properties);
               return velocityViewResolver;
           }
       }