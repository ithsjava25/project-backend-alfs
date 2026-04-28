package org.example.alfs.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.example.alfs.entities.User;
import org.example.alfs.enums.Role;
import org.example.alfs.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  @DisplayName("getAllInvestigators should return all investigators from the repository")
  void getAllInvestigators_returnsInvestigatorsFromRepository() {
    // Arrange
    User investigator = new User();
    investigator.setRole(Role.INVESTIGATOR);
    when(userRepository.findByRole(Role.INVESTIGATOR)).thenReturn(List.of(investigator));

    // Act
    List<User> result = userService.getAllInvestigators();

    // Assert
    assertThat(result).containsExactly(investigator);
    verify(userRepository).findByRole(Role.INVESTIGATOR);
  }

  @Test
  @DisplayName("getAllInvestigators should return an empty list when no investigators exist")
  void getAllInvestigators_whenNoneExist_returnsEmptyList() {
    // Arrange
    when(userRepository.findByRole(Role.INVESTIGATOR)).thenReturn(List.of());

    // Act
    List<User> result = userService.getAllInvestigators();

    // Assert
    assertThat(result).isEmpty();
    verify(userRepository).findByRole(Role.INVESTIGATOR);
  }
}
