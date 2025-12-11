package com.example.basic_pub_sub;

import com.example.basic_pub_sub.publisher.RedisPublisher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publish")
public class PublishController {

    private final RedisPublisher publisher;

    public PublishController(RedisPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/{msg}")
    public String publishMessage(@PathVariable String msg) {
        publisher.publish(msg);
        return "Message published -> " + msg;
    }
}
