package com.jcs.javacommunitysite.atproto.session;

import java.util.Map;

public interface AtprotoAuthSession {
    public String getHandle();
    public String getPdsHost();
    public Map<String, String> getAuthHeaders();
}
