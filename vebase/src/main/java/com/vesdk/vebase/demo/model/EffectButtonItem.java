package com.vesdk.vebase.demo.model;

public class EffectButtonItem extends ButtonItem {
    private ComposerNode node;

    public EffectButtonItem(int icon, int title, ComposerNode node) {
        super(icon, title);
        this.node = node;
    }

    public ComposerNode getNode() {
        return node;
    }

    public void setNode(ComposerNode node) {
        this.node = node;
    }
}
