package org.adaptlab.chpir.android.survey.rules;

public interface RuleCallback {
    // Called by RuleBuilder if all rules pass.
    public void onRulesPass();

    // Called by RuleBuilder if ANY rule fails (immediately).
    public void onRulesFail();
}
