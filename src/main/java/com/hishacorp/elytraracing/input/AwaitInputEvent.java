package com.hishacorp.elytraracing.input;

import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.input.events.InputEvent;

import java.util.function.Consumer;

public class AwaitInputEvent {
    AwaitInputEventType eventType;
    Gui fromGui;
    boolean reopenGui;
    Consumer<InputEvent> action;

    public AwaitInputEvent(AwaitInputEventType eventType, Gui fromGui, boolean reopenGui, Consumer<InputEvent> action) {
        this.eventType = eventType;
        this.fromGui = fromGui;
        this.reopenGui = reopenGui;
        this.action = action;
    }
}
