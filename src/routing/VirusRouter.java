/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

public class VirusRouter extends ActiveRouter {

    private boolean hasSmallVirus = false;
    private boolean hasBigVirus = false;

    public VirusRouter(Settings s) {
        super(s);
    }

    protected VirusRouter(VirusRouter r) {
        super(r);
        this.hasSmallVirus = r.isHasSmallVirus();
        this.hasBigVirus = r.isHasBigVirus();
    }

    public boolean isHasSmallVirus() {
        return hasSmallVirus;
    }

    public void setHasSmallVirus(boolean hasSmallVirus) {
        this.hasSmallVirus = hasSmallVirus;
    }

    public boolean isHasBigVirus() {
        return hasBigVirus;
    }

    public void setHasBigVirus(boolean hasBigVirus) {
        this.hasBigVirus = hasBigVirus;
    }

    @Override
    public void update() {
        super.update();
//		if (isTransferring() || !canStartTransfer()) {
//			return; // transferring, don't try other connections yet
//		}
//
//		// Try first the messages that can be delivered to final recipient
//		if (exchangeDeliverableMessages() != null) {
//			return; // started a transfer, don't try others (yet)
//		}

        // then try any/all message to any/all connection
        this.tryAllMessagesToAllConnections();
    }

    @Override
    public VirusRouter replicate() {
        return new VirusRouter(this);
    }

    @Override
    public boolean createNewMessage(Message m) {
        this.setHasBigVirus(true);
        this.setHasSmallVirus(true);
        return super.createNewMessage(m);
    }

    @Override
    public Message messageTransferred(String id, DTNHost from) {
        Message m = super.messageTransferred(id, from);
        if (((VirusRouter) from.getRouter()).isHasBigVirus()) {
            this.setHasBigVirus(true);
        }
        return m;
    }
}
