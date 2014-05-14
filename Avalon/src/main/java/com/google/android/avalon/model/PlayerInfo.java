package com.google.android.avalon.model;

import java.util.UUID;

/**
* Created by mikewallstedt on 5/12/14.
*/
public class PlayerInfo extends AvalonMessage {
    private static final long serialVersionUID = 1L;

    public final UUID id;
    public String name;

    public PlayerInfo(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerInfo that = (PlayerInfo) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
