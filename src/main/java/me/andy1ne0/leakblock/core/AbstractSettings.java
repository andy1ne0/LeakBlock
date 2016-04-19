package me.andy1ne0.leakblock.core;

import lombok.Getter;

/**
 * Abstract settings class containing common implementation details without being dependant on bukkit or bungee
 *
 * License: LGPLv3
 *
 * @author Janmm14
 * @since 2.0.0
 */
@Getter
public abstract class AbstractSettings implements Settings {

    protected String kickReason;
    protected int maxFailedAttempts;
    protected boolean debug;
    protected boolean updateCheck;
    protected boolean fileCache;
}
