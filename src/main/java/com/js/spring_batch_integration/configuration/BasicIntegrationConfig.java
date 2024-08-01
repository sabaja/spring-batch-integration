package com.js.spring_batch_integration.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

import java.io.File;

/**
 * <a href="https://www.baeldung.com/spring-integration">link per Bealdung</a>
 */
@Slf4j
@Configuration
@EnableIntegration
public class BasicIntegrationConfig {
    public String INPUT_DIR = "/home/sabaja/Desktop/Flowgorithm";
    public String OUTPUT_DIR = "/home/sabaja/Desktop/Casa/Roma";
    public String FILE_PATTERN = "*.ods";

    @Bean
    public MessageChannel fileChannel() {
        return new DirectChannel();
    }

    @Bean
    @InboundChannelAdapter(value = "fileChannel", poller = @Poller(fixedDelay = "5000"))
    public MessageSource<File> fileReadingMessageSource() {
        FileReadingMessageSource sourceReader = new FileReadingMessageSource();
        sourceReader.setDirectory(new File(INPUT_DIR));
        sourceReader.setFilter(new SimplePatternFileListFilter(FILE_PATTERN));
        log.info("Start reading");
        return sourceReader;
    }

    @Bean
    @ServiceActivator(inputChannel = "fileChannel")
    public MessageHandler fileWritingMessageHandler() {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(new File(OUTPUT_DIR));
        handler.setFileExistsMode(FileExistsMode.REPLACE);
        handler.setExpectReply(false);
        log.info("Start writing");
        // Per cancellare il file dopo scritto
        return h -> {
//            log.info("Processing file: [{}]");
            boolean isDelete = ((File) h.getPayload()).delete();
            log.info("delete:[{}]", isDelete);
        };

    }


    /* Esempio con router basato sul contenuto */
    @ServiceActivator(inputChannel = "routingChannel")
    @Bean
    public PayloadTypeRouter router() {
        PayloadTypeRouter router = new PayloadTypeRouter();
        router.setChannelMapping(String.class.getName(), "stringChannel");
        router.setChannelMapping(Integer.class.getName(), "integerChannel");
        return router;
    }
}