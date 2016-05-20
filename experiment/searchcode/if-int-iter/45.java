package org.mobicents.ssf.examples.services.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mobicents.ssf.examples.services.Binding;
import org.mobicents.ssf.examples.services.LocationService;
import org.springframework.stereotype.Component;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LocationServiceImpl implements LocationService {

    private Logger logger = LoggerFactory.getLogger(LocationServiceImpl.class);

    ConcurrentHashMap<String, Bindings> bindingMap = new ConcurrentHashMap<String, Bindings>();

    public Set<Binding> getBindings(String aor) {
        if(logger.isDebugEnabled()) {
            logger.debug("getBindings:aor=" + aor);
        }

        Bindings bindings = bindingMap.get(aor);
        
        if(logger.isDebugEnabled()) {
            logger.debug("getBindings:bindings=" + bindings);
        }
        
        if (bindings == null) {
            return Collections.emptySet();
        }

        return bindings.getBindings();
    }

    public boolean removeBindings(String aor, String callId, String contact,
            int cseq) {
        Bindings bindings = this.getBindingsInternal(aor);
        bindings.remove(aor, callId, contact, cseq);
        return true;
    }

    public boolean updateBinding(String aor, String callId, String contact,
            int cseq, long expirationTime, String state, String misc) {

        if(logger.isDebugEnabled()) {
            logger.debug("updateBinding:aor=" + aor + ",contact=" + contact);
        }

        Bindings bindings = this.getBindingsInternal(aor);
        return bindings.update(aor, callId, contact, cseq, expirationTime,
                state, misc);
    }

    private Bindings getBindingsInternal(String aor) {
        Bindings bindings = bindingMap.get(aor);
        if (bindings == null) {
            bindings = new Bindings();
            Bindings preBindings = bindingMap.putIfAbsent(aor, bindings);
            if (preBindings != null) {
                bindings = preBindings;
            }
        }
        bindings.cleanupExpired();
        return bindings;
    }

    private static class Bindings {
        Set<Binding> bindings = new HashSet<Binding>();

        private AtomicBoolean isLocked = new AtomicBoolean();

        boolean update(String aor, String callid, String contact, int cseq,
                long expirationTime, String state, String misc) {
            lock();

            try {
                boolean isUpdated = false;

                for (Binding binding : bindings) {
                    if (callid.equals(binding.getCallId())) {
                        if (cseq > binding.getCseq()) {
                            System.out.println("cseq=" + cseq
                                    + ",binding.cseq=" + binding.getCseq());
                            binding.setAor(aor);
                            binding.setCallId(callid);
                            binding.setContact(contact);
                            binding.setCseq(cseq);
                            binding.setExpirationTime(expirationTime);
                            binding.setUpdateTime(System.currentTimeMillis());
                            binding.setState(state);
                            binding.setMisc(misc);
                            isUpdated = true;
                            break;
                        } else {
                            throw new IllegalArgumentException();
                        }
                    }
                }

                if (!isUpdated) {
                    // Create the new binding.
                    Binding binding = new Binding();
                    long currentTime = System.currentTimeMillis();
                    binding.setAor(aor);
                    binding.setCallId(callid);
                    binding.setContact(contact);
                    binding.setCseq(cseq);
                    binding.setExpirationTime(expirationTime);
                    binding.setRegistrationTime(currentTime);
                    binding.setUpdateTime(currentTime);
                    binding.setState(state);
                    binding.setMisc(misc);
                    bindings.add(binding);
                }

                return isUpdated;
            } finally {
                unlock();
            }

        }

        int remove(String aor, String callid, String contact, int cseq) {
            int count = 0;

            lock();

            try {
                if (callid == null && contact == null && cseq < 0) {
                    // clear all bindings.
                    bindings.clear();
                } else if (cseq < 0) {
                    // when cseq less than 0, callid & contact must be null.
                    throw new IllegalArgumentException();
                } else {
                    Iterator<Binding> iter = bindings.iterator();
                    while (iter.hasNext()) {
                        Binding binding = iter.next();
                        boolean isDelete = false;
                        if (callid != null) {
                            if (callid.equals(binding.getCallId())
                                    && cseq > binding.getCseq()) {
                                isDelete = true;
                            }
                        }

                        if (isDelete) {
                            iter.remove();
                            count++;
                        }
                    }
                }

                return count;
            } finally {
                unlock();
            }
        }

        void cleanupExpired() {
            lock();
            for (Binding binding : bindings) {
                if (binding.isExpired()) {
                    bindings.remove(binding);
                }
            }
            unlock();
        }

        void lock() {
            while (!isLocked.compareAndSet(false, true)) {
                synchronized (this) {
                    try {
                        wait(100);
                    } catch (Exception e) {
                    }
                }
            }
        }

        void unlock() {
            isLocked.set(false);
        }

        Set<Binding> getBindings() {
            return bindings;
        }
    }

    public List<Binding> getBindings(int count, int page) {
        Collection<Bindings> c = bindingMap.values();
        List<Binding> list = new ArrayList<Binding>();
        for (Bindings bindings : c) {
            Set<Binding> b = bindings.bindings;
            list.addAll(b);
        }
        return list;
    }
}

