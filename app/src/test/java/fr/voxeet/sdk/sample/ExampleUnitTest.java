package fr.voxeet.sdk.sample;

import junit.framework.Assert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.Test;

import java.util.List;

import voxeet.com.sdk.events.success.DispatchMeetingSuccessEvent;
import voxeet.com.sdk.models.impl.DefaultMeeting;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    private List<DefaultMeeting> meetings;

//                meetingService.setUserId("5c0c16eb-6a11-4b20-a242-cc61897b1813");
////                meetingService.deleteMeeting("54b65e0b-b970-488b-85d2-18c70a84e1f4");

    public ExampleUnitTest() {

//        defaultTagService = new DefaultTagService();

        EventBus.getDefault().register(this);
    }

    @Test
    public void toto() {
        Assert.assertEquals(2, 1 + 1);
    }

    @Test
    public void toto3() {
        Assert.assertEquals("michel", "michel");
    }

    @Subscribe
    public void onEvent(DispatchMeetingSuccessEvent event) {
        meetings = event.getMeetings();
    }

//    @Test
//    public void shouldLoadTwoUsers() throw Exception {
//        TestSubscriber<User> testSubscriber = new TestSubscriber<>();
////        databaseHelper.loadUser().subscribe(testSubscriber);
////        testSubscriber.assertNoErrors();
////        testSubscriber.assertReceivedOnNext(Arrays.asList(user1, user2))
//    }

//    @Test
//    public void meeting_getMeetings() throws Exception {
//
//
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.getMeetings();
//    }
//
//    @Test
//    public void meeting_updateSettings() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.updateMeetingSettings("22104f1e-f8a0-43e9-9b6b-f666f8433783", MeetingService.ALL_NOTIFICATION);
//    }
//
//    @Test
//    public void meeting_sendMessage() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.sendMessage("19cc83f9-28f0-4d9b-9a55-795eb404fa8a", "Coco est bete");
//    }
//
//    @Test
//    public void meeting_inviteContact() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.inviteContact("22104f1e-f8a0-43e9-9b6b-f666f8433783", Collections.singletonList("95c2cfb0-c327-45de-9120-6e13ae695ba0"), null);
//    }
//
//    @Test
//    public void meeting_getMeetingFiles() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.getMeetingFiles("22104f1e-f8a0-43e9-9b6b-f666f8433783");
//    }
//
//    @Test
//    public void meeting_updateChannelName() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.updateChannelName("19cc83f9-28f0-4d9b-9a55-795eb404fa8a", "toto52");
//    }
//
//    @Test
//    public void meeting_sendBroadcastCommand() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.sendBroadcastCommand("22104f1e-f8a0-43e9-9b6b-f666f8433783", "TOTO");
//    }
//
//    @Test
//    public void meeting_getMeetingById() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.getMeetingById("fa73bc00-433b-49c2-b2b7-8bda50f35c93");
//    }
//
//    @Test
//    public void meeting_createMeeting() throws Exception {
//        DefaultMeetingService meetingService = Mockito.mock(DefaultMeetingService.class);
//
//        meetingService.createMeeting(null, Collections.singletonList("toto54@yopmail.com"), true);
//    }
//
//    @Test
//    public void addition_isCorrect() throws Exception {
//        assertEquals(4, 2 + 2);
//    }
//
//    @Subscribe
//    public void onEvent(GetMeetingSuccessEvent event) {
//        Log.e("TOTO", "TOTO");
//
//        assertNotNull(event.getMeetings());
//    }

    //                getUploadToken();
//
//                meetingService.getMeetings();
//
//                meetingService.setUserId("5c0c16eb-6a11-4b20-a242-cc61897b1813");
//
////              meetingService.createMeeting(null, Collections.singletonList("toto54@yopmail.com"), true);
//
////                meetingService.deleteMeeting("54b65e0b-b970-488b-85d2-18c70a84e1f4");
//
////                meetingService.getMeetingById("fa73bc00-433b-49c2-b2b7-8bda50f35c93");
//
////                meetingService.sendBroadcastCommand("22104f1e-f8a0-43e9-9b6b-f666f8433783", "TOTO");
//                meetingService.sendBroadcastCommand("19cc83f9-28f0-4d9b-9a55-795eb404fa8a", Event.getJson(new TypingDetection(true)));
//
//                meetingService.updateChannelName("19cc83f9-28f0-4d9b-9a55-795eb404fa8a", "toto52");
//
////                meetingService.getMeetingFiles("22104f1e-f8a0-43e9-9b6b-f666f8433783");
//
////                meetingService.inviteContact("22104f1e-f8a0-43e9-9b6b-f666f8433783", Collections.singletonList("95c2cfb0-c327-45de-9120-6e13ae695ba0"), null);
//
//                meetingService.sendMessage("19cc83f9-28f0-4d9b-9a55-795eb404fa8a", "Coco est bete");
//
//                meetingService.updateMeetingSettings("22104f1e-f8a0-43e9-9b6b-f666f8433783", MeetingService.ALL_NOTIFICATION);
//            }
//
////            getOwnProfile();
//
////            getUploadToken();
//
////            addEmail("michelle1plusbeau@gmail.com");
//
////            List<String> emails = new ArrayList<>();
////            emails.add("toto548@yopmail.com");
////            invite(emails, "toto vous salut");
//
////            getContacts();
//
////            List<String> tags = new ArrayList<>();
////            tags.add("1");
////            tags.add("2");
////            tags.add("3");
////            tags.add("4");
////            tags.add("5");
////
////            addTags("7f2f235a-4e2a-4e2c-90a9-f150084ce988", tags);
////
////            removedTags("7f2f235a-4e2a-4e2c-90a9-f150084ce988", tags);
//
////            getTags();
//
////            createScheduleMeeting(null);
//
////            getScheduledMeetings();
////
////            updateScheduledMeeting("VOXEET_859d67a1-679e-4df8-9fd5-9822a13ad8dd2", null);
////
////            deleteScheduledMeeting("VOXEET_859d67a1-679e-4df8-9fd5-9822a13ad8dd2");
//
////            getFiles();
//
////            getFileById("9d544cba-e5da-11e5-8b72-0242ac110014");
//
////            deleteFiles(Collections.singletonList("9d544cba-e5da-11e5-8b72-0242ac110014"));
//        }
//
//        @Override
//        public void onLoginFail(String error) {
//            Log.e(TAG, error);
//        }
//
//        @Override
//        public void onLogoutSucesss() {
//
//        }
//
//        @Override
//        public void onLogoutFail(String error) {
//
//        }
//
//        @Override
//        public void onResetPasswordSuccess() {
//
//        }
//
//        @Override
//        public void onResetPasswordFail(String error) {
//
//        }
//
//        @Override
//        public void onSignUpSuccess() {
//
//        }
//
//        @Override
//        public void onSignUpFail(String error) {
//
//        }
//
//        @Override
//        public void onAddingEmailSuccess() {
//            setDefaultEmail("michelle1plusbeau@gmail.com");
//        }
//
//        @Override
//        public void onAddingEmailFail(String error) {
//
//        }
//
//        @Override
//        public void onRemovingEmailSuccess() {
//
//        }
//
//        @Override
//        public void onRemovingEmailFail(String error) {
//
//        }
//
//        @Override
//        public void onSettingDefaultEmailSuccess() {
//            removeEmail("michelle1plusbeau@gmail.com");
//        }
//
//        @Override
//        public void onSettingDefaultEmailFail(String error) {
//
//        }
//
//        @Override
//        public void onUploadTokenResponse(String token) {
////            uploadToken = token;
//
////            fileService.setUploadToken(token);
//
////            getScheduledMeetings();
//
////            createScheduleMeeting(null);
//
////            getFiles();
//
////            deleteFiles(Collections.singletonList("6dd8c8fc-afd0-11e5-bada-0242ac11000d"));
//
////            getFileById("toto.jpg", "6dd8c8fc-afd0-11e5-bada-0242ac11000d");
//
////            convertFile("6dd8c8fc-afd0-11e5-bada-0242ac11000d");
//        }
//    };
}