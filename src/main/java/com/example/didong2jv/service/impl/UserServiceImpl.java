package com.example.didong2jv.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.didong2jv.entity.Address;
import com.example.didong2jv.entity.Cart;
import com.example.didong2jv.entity.Role;
import com.example.didong2jv.entity.User;
import com.example.didong2jv.exceptions.APIException;
import com.example.didong2jv.exceptions.ResourceNotFoundException;
import com.example.didong2jv.payloads.AddressDTO;
import com.example.didong2jv.payloads.UserDTO;
import com.example.didong2jv.payloads.UserResponse;
import com.example.didong2jv.repository.AddressRepo;
import com.example.didong2jv.repository.CartRepo;
import com.example.didong2jv.repository.RoleRepo;
import com.example.didong2jv.repository.UserRepo;
import com.example.didong2jv.service.UserService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AddressRepo addressRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO registerUser(UserDTO userDTO, Long roleId) { // 👈 Đã thêm tham số roleId
        try {
            // 1. Convert DTO sang Entity
            User user = modelMapper.map(userDTO, User.class);

            // 2. Xử lý ảnh (nếu có)
            if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
                user.setImage(userDTO.getImage());
            }

            // 3. 🔥 QUAN TRỌNG: Tìm Role theo roleId được truyền vào
            // Nếu Postman gửi 101 -> Tìm ra Role ADMIN
            // Nếu Postman gửi 102 (hoặc ko gửi) -> Tìm ra Role USER
            Role role = roleRepo.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role", "roleId", roleId));
            
            // Thêm role vào danh sách quyền của user
            user.getRoles().add(role);

            // 4. Mã hóa mật khẩu
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // 5. Xử lý địa chỉ (Address) - Logic cũ giữ nguyên
            if (userDTO.getAddress() != null) {
                AddressDTO addrDTO = userDTO.getAddress();
                
                // Kiểm tra xem địa chỉ đã tồn tại trong DB chưa để tái sử dụng
                Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                        addrDTO.getCountry(), addrDTO.getState(), addrDTO.getCity(), 
                        addrDTO.getPincode(), addrDTO.getStreet(), addrDTO.getBuildingName());

                if (address == null) {
                    // Nếu chưa có thì tạo mới
                    address = new Address(null, addrDTO.getStreet(), addrDTO.getBuildingName(), 
                            addrDTO.getCity(), addrDTO.getState(), addrDTO.getCountry(), 
                            addrDTO.getPincode(), null);
                    address = addressRepo.save(address);
                }
                user.setAddresses(List.of(address));
            }

            // 6. Lưu User vào Database
            User registeredUser = userRepo.save(user);

            // 7. Tạo Giỏ hàng (Cart) rỗng cho User mới
            Cart cart = new Cart();
            cart.setUser(registeredUser);
            cart.setTotalPrice(0.0);
            cartRepo.save(cart);

            // 8. Convert Entity ngược lại thành DTO để trả về
            UserDTO responseDTO = modelMapper.map(registeredUser, UserDTO.class);
            
            if (!registeredUser.getAddresses().isEmpty()) {
                responseDTO.setAddress(modelMapper.map(registeredUser.getAddresses().get(0), AddressDTO.class));
            }
            return responseDTO;

        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }
    }

    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<User> pageUsers = userRepo.findAll(pageDetails);
        List<User> users = pageUsers.getContent();

        if (users.isEmpty()) throw new APIException("No User exists !!!");

        List<UserDTO> userDTOs = users.stream().map(u -> {
            UserDTO dto = modelMapper.map(u, UserDTO.class);
            if (!u.getAddresses().isEmpty()) dto.setAddress(modelMapper.map(u.getAddresses().get(0), AddressDTO.class));
            return dto;
        }).collect(Collectors.toList());

        UserResponse userResponse = new UserResponse();
        userResponse.setContent(userDTOs);
        userResponse.setPageNumber(pageUsers.getNumber());
        userResponse.setPageSize(pageUsers.getSize());
        userResponse.setTotalElements(pageUsers.getTotalElements());
        userResponse.setTotalPages(pageUsers.getTotalPages());
        userResponse.setLastPage(pageUsers.isLast());
        return userResponse;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        if (!user.getAddresses().isEmpty()) dto.setAddress(modelMapper.map(user.getAddresses().get(0), AddressDTO.class));
        return dto;
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        if (!user.getAddresses().isEmpty()) {
            dto.setAddress(modelMapper.map(user.getAddresses().get(0), AddressDTO.class));
        }
        return dto;
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobileNumber(userDTO.getMobileNumber());
        user.setEmail(userDTO.getEmail());

        if (userDTO.getImage() != null && !userDTO.getImage().isEmpty()) {
            user.setImage(userDTO.getImage());
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        if (userDTO.getAddress() != null) {
            AddressDTO aDto = userDTO.getAddress();
            Address address = addressRepo.findByCountryAndStateAndCityAndPincodeAndStreetAndBuildingName(
                    aDto.getCountry(), aDto.getState(), aDto.getCity(), 
                    aDto.getPincode(), aDto.getStreet(), aDto.getBuildingName());
            if (address == null) {
                address = new Address(null, aDto.getStreet(), aDto.getBuildingName(), 
                        aDto.getCity(), aDto.getState(), aDto.getCountry(), aDto.getPincode(), null);
                address = addressRepo.save(address);
            }
            user.setAddresses(List.of(address));
        }
        user = userRepo.save(user);
        UserDTO responseDTO = modelMapper.map(user, UserDTO.class);
        if (!user.getAddresses().isEmpty()) responseDTO.setAddress(modelMapper.map(user.getAddresses().get(0), AddressDTO.class));
        return responseDTO;
    }

    @Override
    public String deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        
        // Cần cẩn thận khi xóa User có dính líu đến Cart hoặc Order.
        // Spring Data JPA sẽ tự xử lý nếu bạn cấu hình Cascade Type ở Entity đúng.
        userRepo.delete(user);
        return "User with userId " + userId + " deleted successfully!!!";
    }
}