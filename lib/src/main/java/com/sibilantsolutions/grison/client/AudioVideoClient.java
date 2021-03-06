package com.sibilantsolutions.grison.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import com.sibilantsolutions.grison.driver.foscam.dto.CommandDto;
import com.sibilantsolutions.grison.rx.State;
import com.sibilantsolutions.grison.rx.event.action.AbstractAction;
import com.sibilantsolutions.grison.rx.event.result.AbstractResult;
import com.sibilantsolutions.grison.rx.event.result.AudioVideoReceiveResult;
import com.sibilantsolutions.grison.rx.event.result.OperationReceiveResult;
import com.sibilantsolutions.grison.rx.event.ui.ConnectUiEvent;
import com.sibilantsolutions.grison.rx.event.ui.UiEvent;
import com.sibilantsolutions.grison.rx.event.xform.AbstractActionToAbstractResult;
import com.sibilantsolutions.grison.rx.event.xform.CommandToAudioVideoReceiveResult;
import com.sibilantsolutions.grison.rx.event.xform.CommandToOperationReceiveResult;
import com.sibilantsolutions.grison.rx.event.xform.StateAndResultToStateBiFunction;
import com.sibilantsolutions.grison.rx.event.xform.UiEventToAbstractAction;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.processors.FlowableProcessor;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

@Service
@EnableConfigurationProperties(CameraConnectionProperties.class)
public class AudioVideoClient {

    private final CameraConnectionProperties cameraConnectionProperties;

    public AudioVideoClient(CameraConnectionProperties cameraConnectionProperties) {
        this.cameraConnectionProperties = cameraConnectionProperties;
    }

    public Flowable<State> stream() {

        Flowable<UiEvent> events = Flowable.just(new ConnectUiEvent(cameraConnectionProperties.getHost(), cameraConnectionProperties.getPort()));

        //TODO: Handle upstream socket disconnect.

        Flowable<AbstractAction> uiEventsToActions = events
                .compose(new UiEventToAbstractAction());

        final FlowableProcessor<AbstractAction> dynamicActions = PublishProcessor.<AbstractAction>create().toSerialized();

        Flowable<AbstractAction> actions = Flowable
                .merge(
                        uiEventsToActions,
                        dynamicActions);

        final FlowableProcessor<CommandDto> operationDatastream = PublishProcessor.<CommandDto>create().toSerialized();
        final FlowableProcessor<CommandDto> audioVideoDatastream = PublishProcessor.<CommandDto>create().toSerialized();

        final Flowable<OperationReceiveResult> operationReceiveResults = operationDatastream
                .observeOn(Schedulers.io())
                .compose(new CommandToOperationReceiveResult());

        final Flowable<AudioVideoReceiveResult> audioVideoReceiveResults = audioVideoDatastream
                .onBackpressureLatest()
                .observeOn(Schedulers.io())
                .compose(new CommandToAudioVideoReceiveResult());

        final Flowable<AbstractResult> actionResults = actions
                .compose(new AbstractActionToAbstractResult(operationDatastream, audioVideoDatastream));

        final Flowable<AbstractResult> results = Flowable.merge(
                actionResults,
                operationReceiveResults,
                audioVideoReceiveResults);

        return results
                .scanWith(
                        State::init,
                        new StateAndResultToStateBiFunction(dynamicActions, cameraConnectionProperties.getUsername(), cameraConnectionProperties.getPassword()));
    }

}
