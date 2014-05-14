package com.google.android.avalon.rules;

import com.google.android.avalon.model.AvalonRole;
import com.google.android.avalon.model.GameConfiguration;
import com.google.android.avalon.model.InitialAssignments;
import com.google.android.avalon.model.PlayerName;
import com.google.android.avalon.model.RoleAssignment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
* Created by mikewallstedt on 5/13/14.
*/
public class AssignmentFactory {
    private final GameConfiguration mGameConfiguration;

    public AssignmentFactory(GameConfiguration mGameConfiguration) {
        this.mGameConfiguration = mGameConfiguration;
    }

    public InitialAssignments getAssignments(List<PlayerName> players) {
        List<AvalonRole> rolesInPlay = getRolesInPlay();
        if (players.size() != rolesInPlay.size()) {
            throw new IllegalStateException(
                    String.format("Cannot assign %d roles to %d players.",
                            rolesInPlay.size(), players.size()));
        }
        Collections.shuffle(rolesInPlay);

        Map<PlayerName, AvalonRole> playerToRole = new HashMap<PlayerName, AvalonRole>();
        for (int i = 0; i < players.size(); i++) {
            playerToRole.put(players.get(i), rolesInPlay.get(i));
        }

        Set<RoleAssignment> assignments = new HashSet<RoleAssignment>();
        for (PlayerName player: players) {
            Set<PlayerName> seenBy = getSeenBy(playerToRole, player);
            assignments.add(new RoleAssignment(player, playerToRole.get(player), seenBy));
        }

        int kingIndex = new Random().nextInt(players.size());
        PlayerName king = players.get(kingIndex);
        PlayerName lady = null;
        if (mGameConfiguration.enableLadyOfTheLake) {
            int ladyIndex = kingIndex == 0 ? players.size() - 1 : kingIndex - 1;
            lady = players.get(ladyIndex);
        }

        return new InitialAssignments(assignments, king, lady);
    }

    // VisibleForTesting
    List<AvalonRole> getRolesInPlay() {
        if (mGameConfiguration.numPlayers < 5) {
            throw new IllegalStateException(
                    String.format("Too few players. 5 required, only %d provided.",
                            mGameConfiguration.numPlayers));
        }
        int numEvil = mGameConfiguration.numPlayers % 3 == 0 ?
                mGameConfiguration.numPlayers / 3 : mGameConfiguration.numPlayers / 3 + 1;
        Collection<AvalonRole> specialEvil = getSpecialEvil();
        if (specialEvil.size() > numEvil) {
            String message = String.format(
                "Too many special evil roles requested. A game with %d players, supports %d " +
                        "evil players. %d were requested.",
                    mGameConfiguration.numPlayers, numEvil, specialEvil.size());
            throw new IllegalStateException(message);
        }

        List<AvalonRole> rolesInPlay = new ArrayList<AvalonRole>();
        rolesInPlay.addAll(specialEvil);
        for (int i = rolesInPlay.size(); i < numEvil; i++) {
            rolesInPlay.add(AvalonRole.MINION);
        }
        rolesInPlay.addAll(getSpecialGood());
        for (int i = rolesInPlay.size(); i < mGameConfiguration.numPlayers; i++) {
            rolesInPlay.add(AvalonRole.LOYAL);
        }
        return rolesInPlay;
    }

    private Collection<AvalonRole> getSpecialEvil() {
        Set<AvalonRole> result = new HashSet<AvalonRole>();
        if (mGameConfiguration.includeAssassin) {
            result.add(AvalonRole.ASSASSIN);
        }
        if (mGameConfiguration.includeMordred) {
            result.add(AvalonRole.MORDRED);
        }
        if (mGameConfiguration.includeMorgana) {
            result.add(AvalonRole.MORGANA);
        }
        if (mGameConfiguration.includeOberon) {
            result.add(AvalonRole.OBERON);
        }
        return result;
    }

    private Collection<AvalonRole> getSpecialGood() {
        Set<AvalonRole> result = new HashSet<AvalonRole>();
        if (mGameConfiguration.includeMerlin) {
            result.add(AvalonRole.MERLIN);
        }
        if (mGameConfiguration.includePercival) {
            result.add(AvalonRole.PERCIVAL);
        }
        return result;
    }

    private Set<PlayerName> getSeenBy(Map<PlayerName, AvalonRole> playerToRole, PlayerName player) {
        Set<PlayerName> seenBy = new HashSet<PlayerName>();
        switch(playerToRole.get(player)) {
            case MERLIN:
                for (PlayerName p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (!r.isGood && !r.equals(AvalonRole.MORDRED)) {
                        seenBy.add(p);
                    }
                }
                break;
            case PERCIVAL:
                for (PlayerName p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (r.equals(AvalonRole.ASSASSIN.MERLIN) || r.equals(AvalonRole.MORGANA)) {
                        seenBy.add(p);
                    }
                }
                break;
            case MINION:
            case ASSASSIN:
            case MORDRED:
            case MORGANA:
                for (PlayerName p: playerToRole.keySet()) {
                    AvalonRole r = playerToRole.get(p);
                    if (!r.isGood && !r.equals(AvalonRole.OBERON) && p != player) {
                        seenBy.add(p);
                    }
                }
                break;
            // LOYAL and OBERON see nothing.
        }
        return seenBy;
    }
}
