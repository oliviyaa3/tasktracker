package com.oliviya.tasktracker.service;

import com.oliviya.tasktracker.payload.UserDTO;
import com.oliviya.tasktracker.payload.UserResponse;

public interface UserService {

    UserDTO createUser(UserDTO userDTO);

    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    UserDTO getUserById(Long id);

    UserDTO updateUser(UserDTO userDTO, Long id);

    UserDTO deleteUser(Long id);

}
