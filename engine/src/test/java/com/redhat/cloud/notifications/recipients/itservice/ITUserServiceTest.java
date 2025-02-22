package com.redhat.cloud.notifications.recipients.itservice;

import com.redhat.cloud.notifications.recipients.User;
import com.redhat.cloud.notifications.recipients.itservice.pojo.request.ITUserRequest;
import com.redhat.cloud.notifications.recipients.itservice.pojo.response.AccountRelationship;
import com.redhat.cloud.notifications.recipients.itservice.pojo.response.Authentication;
import com.redhat.cloud.notifications.recipients.itservice.pojo.response.Email;
import com.redhat.cloud.notifications.recipients.itservice.pojo.response.ITUserResponse;
import com.redhat.cloud.notifications.recipients.itservice.pojo.response.PersonalInformation;
import com.redhat.cloud.notifications.recipients.rbac.RbacRecipientUsersProvider;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ITUserServiceTest {

    @Inject
    RbacRecipientUsersProvider rbacRecipientUsersProvider;

    @InjectMock
    @RestClient
    ITUserService itUserService;

    @Test
    void shouldPickPrimaryEMailAsUsersEmail() {
        ITUserResponse itUserResponse = new ITUserResponse();

        final PersonalInformation personalInformation = new PersonalInformation();
        personalInformation.firstName = "myFirstname";
        personalInformation.lastNames = "myLastname";
        itUserResponse.personalInformation = personalInformation;

        Authentication authentication = new Authentication();
        authentication.principal = "myPrincipal";
        authentication.providerName = "myProviderName";
        itUserResponse.authentications = new ArrayList<>();
        itUserResponse.authentications.add(authentication);

        final AccountRelationship accountRelationship = new AccountRelationship();

        Email primaryEmail = new Email();
        primaryEmail.isPrimary = true;
        primaryEmail.address = "first_adress@trashmail.org";

        Email nonPrimaryEmail = new Email();
        nonPrimaryEmail.isPrimary = false;
        nonPrimaryEmail.address = "second_adress@trashmail.org";

        accountRelationship.emails = new ArrayList<>();
        accountRelationship.emails.add(nonPrimaryEmail);
        accountRelationship.emails.add(primaryEmail);
        itUserResponse.accountRelationships = new ArrayList<>();
        itUserResponse.accountRelationships.add(accountRelationship);
        itUserResponse.accountRelationships.get(0).permissions = List.of();
        List<ITUserResponse> itUserResponses = List.of(itUserResponse);

        when(itUserService.getUsers(any(ITUserRequest.class))).thenReturn(itUserResponses);

        final List<User> someAccountId = rbacRecipientUsersProvider.getUsers("someAccountId", "someOrgId", true);
        assertTrue(someAccountId.get(0).isActive());

        assertEquals(someAccountId.get(0).getEmail(), "first_adress@trashmail.org");
    }

    @Test
    void shouldMapUsersCorrectly() {
        final RbacRecipientUsersProvider mock = Mockito.mock(RbacRecipientUsersProvider.class);
        User mockedUser = createNonAdminMockedUser();
        List<User> mockedUsers = List.of(mockedUser);

        when(mock.getUsers(anyString(), anyString(), anyBoolean())).thenReturn(mockedUsers);
        final List<User> users = mock.getUsers("someAccountId", "someOrgId", false);

        final User user = users.get(0);
        assertEquals("firstName", user.getFirstName());
        assertEquals("lastName", user.getLastName());
        assertEquals("userName", user.getUsername());
        assertEquals("email@trashmail.xyz", user.getEmail());
        assertTrue(user.isActive());
        assertFalse(user.isAdmin());
    }

    private User createNonAdminMockedUser() {
        User mockedUser = new User();
        mockedUser.setActive(true);
        mockedUser.setLastName("lastName");
        mockedUser.setFirstName("firstName");
        mockedUser.setUsername("userName");
        mockedUser.setEmail("email@trashmail.xyz");
        mockedUser.setAdmin(false);
        mockedUser.setActive(true);
        return mockedUser;
    }
}
