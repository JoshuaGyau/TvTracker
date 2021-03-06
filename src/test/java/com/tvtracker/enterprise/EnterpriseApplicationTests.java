package com.tvtracker.enterprise;

import com.tvtracker.enterprise.dto.MediaEntry;
import com.tvtracker.enterprise.dto.UserAccount;
import com.tvtracker.enterprise.service.IMediaEntryService;
import com.tvtracker.enterprise.service.IUserAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

@SpringBootTest
class EnterpriseApplicationTests {
    @Autowired
    IUserAccountService userAccountService;
    @Autowired
    IMediaEntryService mediaEntryService;

    final String TEST_USERNAME = "testUser";
    final String TEST_USER_PASSWORD = "testPassword";

    @Test
    void contextLoads() {
    }

    @Test
    void userCreatesUserAccount_ReturnsValidAuthenticationToken() throws Exception {
        UserAccount userAccount = whenUserSendsUserAccountWithUniqueUsername();
        returnsUserAccountWithValidToken(userAccount);
    }

    private UserAccount whenUserSendsUserAccountWithUniqueUsername() throws Exception {
        UserAccount userAccount = new UserAccount();
        userAccount.setUsername(TEST_USERNAME);
        userAccount.setPassword(TEST_USER_PASSWORD);
        userAccount.setEmail("testUser@testSite.com");

        UserAccount newUserAccount = userAccountService.createUserAccount(userAccount);

        Assert.notNull(newUserAccount, "Creating user account returned null indicating username is not unique.");

        return newUserAccount;
    }

    private void returnsUserAccountWithValidToken(UserAccount userAccount) throws Exception {
        Assert.notNull(userAccount, "Creating user account returned null indicating username is not unique.");

        boolean isValid = userAccountService.isTokenValid(userAccount.getToken(), userAccount.getUsername());

        Assert.isTrue(isValid, "Token returned from user account creation was not valid.");
    }

    @Test
    void userCreatesMediaEntry_NewMediaEntryHasEntryId() throws Exception {
        whenUserCreatesNewMediaEntry();
        newMediaEntryHasEntryId();
    }

    private void whenUserCreatesNewMediaEntry() throws Exception {
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setType("Movie");
        mediaEntry.setUsername(TEST_USERNAME);
        mediaEntry.setTitle("Easy Rider");
        mediaEntry.setDescription("Instant classic!");
        mediaEntry.setWatched(false);
        mediaEntryService.createMediaEntry(mediaEntry);
    }

    private void newMediaEntryHasEntryId() throws Exception {
        List<MediaEntry> entries = mediaEntryService.fetchMediaEntriesByUsername(TEST_USERNAME);
        MediaEntry mediaEntry = entries.get(0);

        boolean isCorrectEntry = mediaEntry.getDescription().equals("Instant classic!") && mediaEntry.getTitle().equals("Easy Rider");
        Assert.isTrue(isCorrectEntry, "Media Entry values do not match the created media entry.");
        Assert.isTrue(mediaEntry.getEntryId() > -1, "Media Entry was not given a valid id.");
    }

    @Test
    void userUpdatesMediaEntry_ReturnsSuccessBoolean() throws Exception {
        givenUserHasCreatedAMediaEntry();
        int entryId = whenUserUpdatesMediaEntry();
        returnIndicationOfSuccess(entryId);
    }

    private void givenUserHasCreatedAMediaEntry() throws Exception {
        MediaEntry mediaEntry = new MediaEntry();
        mediaEntry.setType("Movie");
        mediaEntry.setUsername(TEST_USERNAME);
        mediaEntry.setTitle("Easy Rider");
        mediaEntry.setDescription("Instant classic!");
        mediaEntry.setWatched(false);
        mediaEntryService.createMediaEntry(mediaEntry);

        List<MediaEntry> mediaEntries = mediaEntryService.fetchMediaEntriesByUsername(TEST_USERNAME);

        Assert.notNull(mediaEntries, "User's collection of media entries was null.");
        Assert.noNullElements(mediaEntries, "A null element was found in user's collection of media entries.");
        Assert.notEmpty(mediaEntries, "User's collection of media entries was empty.");
    }

    private int whenUserUpdatesMediaEntry() throws Exception {
        List<MediaEntry> mediaEntries = mediaEntryService.fetchMediaEntriesByUsername(TEST_USERNAME);
        // update a media entry
        MediaEntry mediaEntry = mediaEntries.get(0);
        MediaEntry newMediaEntry = new MediaEntry();
        newMediaEntry.setEntryId(mediaEntry.getEntryId());
        newMediaEntry.setUsername(mediaEntry.getUsername());
        newMediaEntry.setTitle(mediaEntry.getTitle());
        newMediaEntry.setType(mediaEntry.getType());
        newMediaEntry.setDescription("Terrible");
        newMediaEntry.setPlatform("Hulu");
        newMediaEntry.setWatched(true);

        boolean success = mediaEntryService.updateMediaEntry(newMediaEntry);
        Assert.isTrue(success, "An error occurred while updating a media entry.");

        return newMediaEntry.getEntryId();
    }

    private void returnIndicationOfSuccess(int entryId) throws Exception {
        // check if media entry was successfully updated
        List<MediaEntry> mediaEntries = mediaEntryService.fetchMediaEntriesByUsername(TEST_USERNAME);
        MediaEntry foundEntry = null;
        for(MediaEntry entry: mediaEntries) if (entry.getEntryId() == entryId) foundEntry = entry;

        Assert.notNull(foundEntry, "The updated media entry was not found.");
        boolean updated = foundEntry.isWatched() && foundEntry.getPlatform().equals("Hulu") && foundEntry.getDescription().equals("Terrible");
        Assert.isTrue(updated, "Media entry's fields were not updated.");
    }
}
