package ar.uba.fi.ingsoft1.sistema_comedores.user.profile;

import org.springframework.stereotype.Service;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ImageService;
import ar.uba.fi.ingsoft1.sistema_comedores.image.ObjectPrefixConsts;
import ar.uba.fi.ingsoft1.sistema_comedores.user.User;
import ar.uba.fi.ingsoft1.sistema_comedores.user.UserRepository;
import ar.uba.fi.ingsoft1.sistema_comedores.user.exception.UserNotFoundException;

import org.springframework.web.multipart.MultipartFile;

@Service
public class UserProfileService {

    private final UserRepository userRepository;
    private final ImageService imageService;

    public UserProfileService(UserRepository userRepository, ImageService imageService) {
        this.userRepository = userRepository;
        this.imageService = imageService;
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
    }


    public UserProfileDTO getUserProfile(String email) {
        try {
            User user = findUserByEmail(email);

            return new UserProfileDTO(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.isActive(),                
                user.getBirthDate(),
                user.getGender().toString(),
                user.getAddress(),
                user.getProfileImageUrl()
            );

        } catch (Exception e) {
            throw new UserNotFoundException("Error al obtener perfil: " + e.getMessage());
        }
    }

    public void updateUserProfile(String email, UserProfileUpdateDTO updateDTO) {
        User user = findUserByEmail(email);

        if (updateDTO.firstName() != null) user.setFirstName(updateDTO.firstName());
        if (updateDTO.lastName() != null) user.setLastName(updateDTO.lastName());
        if (updateDTO.birthDate() != null) user.setBirthDate(updateDTO.birthDate());
        if (updateDTO.gender() != null) user.setGender(updateDTO.gender());
        if (updateDTO.address() != null) user.setAddress(updateDTO.address());

        userRepository.save(user);
    }

    public void updateProfilePhoto(String email, MultipartFile file) {
        User user = findUserByEmail(email);
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
            String previousImage = user.getProfileImageUrl();
            imageService.deleteImage(previousImage);
        }
        String objectPrefix = String.format("%s/%s%s", ObjectPrefixConsts.USERS_OBJECT_PREFIX, user.getId(), ObjectPrefixConsts.PROFILE_OBJECT_PREFIX);
        String actualProfileImageUrl = imageService.uploadImage(file, objectPrefix);
        user.setProfileImageUrl(actualProfileImageUrl);
        userRepository.save(user);
    }

}
