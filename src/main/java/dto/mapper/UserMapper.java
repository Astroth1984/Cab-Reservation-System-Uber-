package dto.mapper;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import dto.model.user.RoleDto;
import dto.model.user.UserDto;
import model.user.User;


@Component
public class UserMapper {
	
	 public static UserDto toUserDto(User user) {
	        return new UserDto()
	                .setEmail(user.getEmail())
	                .setFirstName(user.getFirstName())
	                .setLastName(user.getLastName())
	                .setMobileNumber(user.getMobileNumber())
	                .setRoles(new HashSet<RoleDto>(user
	                        .getRoles()
	                        .stream()
	                        .map(role -> new ModelMapper().map(role, RoleDto.class))
	                        .collect(Collectors.toSet())));
	    }

}
