package com.quick.ext.primefaces.base.util;

import java.util.UUID;
import java.util.Vector;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.Session;

/**
 *
 * @GeneratedValue(generator = "system-uuid")
 * @author Jason
 */
@SuppressWarnings("serial")
public class UUIDSequence extends Sequence implements SessionCustomizer {

    public UUIDSequence() {
        super();
    }

    public UUIDSequence(String name) {
        super(name);
    }

    @Override
    public String getGeneratedValue(Accessor accessor,
            AbstractSession writeSession, String seqName) {
        return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Vector getGeneratedVector(Accessor accessor,
            AbstractSession writeSession, String seqName, int size) {
        return null;
    }

    @Override
    public boolean shouldAcquireValueAfterInsert() {
        return false;
    }

    @Override
    public boolean shouldUseTransaction() {
        return false;
    }

    @Override
    public boolean shouldUsePreallocation() {
        return false;
    }

    @Override
    public void customize(Session session) throws Exception {
        UUIDSequence sequence = new UUIDSequence("system-uuid");

        session.getLogin().addSequence(sequence);
    }

    @Override
    public void onConnect() {
    }

    @Override
    public void onDisconnect() {
    }
}
