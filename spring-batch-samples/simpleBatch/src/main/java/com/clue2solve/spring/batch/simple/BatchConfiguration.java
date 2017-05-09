package com.clue2solve.spring.batch.simple;

import com.clue2solve.spring.batch.simple.model.Person;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    @Bean
    @StepScope
    FlatFileItemReader<Person> flatFileItemReader(@Value("#{jobParameters[file]}") File file) {
        FlatFileItemReader<Person> r = new FlatFileItemReader<Person>();
        r.setResource(new FileSystemResource(file));
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<Person>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setNames(new String[]{"first", "last", "email"});
        lineMapper.setLineTokenizer(tokenizer);
        BeanWrapperFieldSetMapper<Person> fieldSetMapper = new BeanWrapperFieldSetMapper<Person>();
        fieldSetMapper.setTargetType(Person.class);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        r.setLineMapper(lineMapper);
        return r;
    }

    @Bean
    JdbcBatchItemWriter<Person> jdbcBatchItemWriter(DataSource h2) {
        JdbcBatchItemWriter<Person> w = new JdbcBatchItemWriter<>();
        w.setDataSource(h2);
        w.setSql("insert into PEOPLE (first, last , email) values (:first , :last , :email)");
        w.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Person>());
        return w;
    }

    @Bean
    Job personEtl(JobBuilderFactory jobBuilderFactory,
                  StepBuilderFactory stepBuilderFactory,
                  FlatFileItemReader<Person> flatFileItemReader,
                  JdbcBatchItemWriter<Person> jdbcBatchItemWriter
    ) {
        Step step = stepBuilderFactory.get("file-to-datababase")
                .<Person, Person>chunk(5)
                .reader(flatFileItemReader)
                .writer(jdbcBatchItemWriter)
                .build();
        return jobBuilderFactory.get("etl")
                .start(step)
                .build();

    }

    @Bean
    CommandLineRunner runner(JobLauncher jobLauncher,
                             Job job,
                             @Value("${file}") File in,
                             JdbcTemplate jdbcTemplate
    ) {
        return args -> {
            JobExecution execution = jobLauncher.run(job,
                    new JobParametersBuilder()
                            .addString("file", in.getAbsolutePath())
                            .toJobParameters());
            System.out.println("Execution Status: " + execution.getExitStatus().toString());
            List<Person> personList = jdbcTemplate.query("select * from PEOPLE", (resultSet, i) -> {
                return new Person(resultSet.getString("first"),
                        resultSet.getString("last"),
                        resultSet.getString("email"));
            });

            personList.forEach(System.out::println);

        };
    }
}
