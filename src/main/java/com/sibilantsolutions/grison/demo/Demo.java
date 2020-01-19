package com.sibilantsolutions.grison.demo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sibilantsolutions.grison.client.AudioVideoClient;
import com.sibilantsolutions.grison.evt.AudioHandlerI;
import com.sibilantsolutions.grison.rx.State;
import com.sibilantsolutions.grison.rx.client.OpClientImpl;
import com.sibilantsolutions.grison.rx.net.ChannelSender;
import io.netty.channel.Channel;
import io.reactivex.Flowable;

public class Demo {
    private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

    private static final AudioHandlerI audioHandler = new DemoAudioHandler();
    private static final JLabel imageLabel = new JLabel();
    private static final JLabel uptimeLabel = new JLabel();
    private static final JLabel timestampLabel = new JLabel();
    private static final JLabel fpsLabel = new JLabel();
    private static final JButton videoEndButton = new JButton("Video End");
    private static final JButton videoStartButton = new JButton("Video Start");
    private static final JButton audioEndButton = new JButton("Audio End");
    private static final JButton audioStartButton = new JButton("Audio Start");
    private static final DemoImageHandler imageHandler = new DemoImageHandler();

    static {
        imageHandler.setImageLabel(imageLabel);
        imageHandler.setUptimeLabel(uptimeLabel);
        imageHandler.setTimestampLabel(timestampLabel);
        imageHandler.setFpsLabel(fpsLabel);
    }

    static public void demo(final String hostname, final int port, final String username, final String password) {
        DemoUi.buildUi(imageLabel, uptimeLabel, timestampLabel, fpsLabel, videoStartButton, videoEndButton,
                audioStartButton, audioEndButton);

        final MyVideoStartActionListener videoStartActionListener = new MyVideoStartActionListener();
        videoStartButton.addActionListener(videoStartActionListener);

        final MyVideoEndActionListener videoEndActionListener = new MyVideoEndActionListener();
        videoEndButton.addActionListener(videoEndActionListener);

        final MyAudioStartActionListener audioStartActionListener = new MyAudioStartActionListener();
        audioStartButton.addActionListener(audioStartActionListener);

        final MyAudioEndActionListener audioEndActionListener = new MyAudioEndActionListener();
        audioEndButton.addActionListener(audioEndActionListener);

        final Flowable<State> stateFlowable = AudioVideoClient.stream(hostname, port, username, password);

        stateFlowable
                .subscribe(
                        state -> {
                            if (state.videoDataText != null) {
                                imageHandler.onReceive(state.videoDataText);
                            }

                            if (state.audioDataText != null) {
                                audioHandler.onReceive(state.audioDataText);
                            }

                            videoStartActionListener.operationChannel = state.operationChannel;
                            videoEndActionListener.operationChannel = state.operationChannel;
                            audioStartActionListener.operationChannel = state.operationChannel;
                            audioEndActionListener.operationChannel = state.operationChannel;
                        },
                        throwable -> {
                            LOG.error("Trouble: ", throwable);
                            imageHandler.onVideoStopped();
                        },
                        imageHandler::onVideoStopped);
    }

    private static class MyVideoEndActionListener implements ActionListener {

        private Channel operationChannel;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (operationChannel != null) {
                new OpClientImpl(new ChannelSender(operationChannel)).videoEnd().subscribe();
            }
            imageHandler.onVideoStopped();
        }
    }

    private static class MyVideoStartActionListener implements ActionListener {

        private Channel operationChannel;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (operationChannel != null) {
                new OpClientImpl(new ChannelSender(operationChannel)).videoStart().subscribe();
            }

        }
    }

    private static class MyAudioStartActionListener implements ActionListener {

        private Channel operationChannel;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (operationChannel != null) {
                new OpClientImpl(new ChannelSender(operationChannel)).audioStart().subscribe();
            }
        }
    }

    private static class MyAudioEndActionListener implements ActionListener {

        private Channel operationChannel;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (operationChannel != null) {
                new OpClientImpl(new ChannelSender(operationChannel)).audioEnd().subscribe();
            }
        }
    }

}
