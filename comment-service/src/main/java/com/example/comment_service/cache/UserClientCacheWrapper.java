package com.example.comment_service.cache;

import com.example.comment_service.client.UserClient;
import com.example.comment_service.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserClientCacheWrapper {
    @Autowired
    private UserClient userClient;

    @Cacheable(value = "userCache", key = "#id")
    public UserDTO getUserById(String id) {
        return userClient.getUserById(id);
    }

    @Cacheable(value = "userBulkCache", key = "#ids.hashCode()")
    public List<UserDTO> getUserByIds(List<String> ids) {
        return userClient.getUserByIds(ids);
    }
}
