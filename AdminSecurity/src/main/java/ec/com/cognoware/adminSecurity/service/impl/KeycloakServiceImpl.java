package ec.com.cognoware.adminSecurity.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ec.com.cognoware.adminSecurity.dto.UserDTO;
import ec.com.cognoware.adminSecurity.service.IKeycloakService;
import ec.com.cognoware.adminSecurity.util.keycloakProvider;
import jakarta.ws.rs.core.Response;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
@Slf4j
public class KeycloakServiceImpl implements IKeycloakService {



        public List<UserRepresentation> findAllUsers(){
            return keycloakProvider.getRealmResource()
                    .users()
                    .list();
        }



        public List<UserRepresentation> searchUserByUsername(String username) {
            return keycloakProvider.getRealmResource()
                    .users()
                    .searchByUsername(username, true);
        }



        public String createUser(@NonNull UserDTO userDTO) {

            int status = 0;
            UsersResource usersResource = keycloakProvider.getUserResource();

            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setFirstName(userDTO.getFirstName());
            userRepresentation.setLastName(userDTO.getLastName());
            userRepresentation.setEmail(userDTO.getEmail());
            userRepresentation.setUsername(userDTO.getUsername());
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(true);

            Response response = usersResource.create(userRepresentation);

            status = response.getStatus();

            if (status == 201) {
                String path = response.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf("/") + 1);

                CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
                credentialRepresentation.setTemporary(false);
                credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
                credentialRepresentation.setValue(userDTO.getPassword());

                usersResource.get(userId).resetPassword(credentialRepresentation);

                RealmResource realmResource = keycloakProvider.getRealmResource();

                List<RoleRepresentation> rolesRepresentation = null;

                if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
                    rolesRepresentation = List.of(realmResource.roles().get("user").toRepresentation());
                } else {
                    rolesRepresentation = realmResource.roles()
                            .list()
                            .stream()
                            .filter(role -> userDTO.getRoles()
                                    .stream()
                                    .anyMatch(roleName -> roleName.equalsIgnoreCase(role.getName())))
                            .toList();
                }

                realmResource.users().get(userId).roles().realmLevel().add(rolesRepresentation);

                return "User created successfully!!";

            } else if (status == 409) {
                log.error("User exist already!");
                return "User exist already!";
            } else {
                log.error("Error creating user, please contact with the administrator.");
                return "Error creating user, please contact with the administrator.";
            }
        }



        public void deleteUser(String userId){
            keycloakProvider.getUserResource()
                    .get(userId)
                    .remove();
        }



        public void updateUser(String userId, @NonNull UserDTO userDTO){

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setTemporary(false);
            credentialRepresentation.setType(OAuth2Constants.PASSWORD);
            credentialRepresentation.setValue(userDTO.getPassword());

            UserRepresentation user = new UserRepresentation();
            user.setUsername(userDTO.getUsername());
            user.setFirstName(userDTO.getFirstName());
            user.setLastName(userDTO.getLastName());
            user.setEmail(userDTO.getEmail());
            user.setEnabled(true);
            user.setEmailVerified(true);
            user.setCredentials(Collections.singletonList(credentialRepresentation));

            UserResource usersResource = keycloakProvider.getUserResource().get(userId);
            usersResource.update(user);
        }
}
