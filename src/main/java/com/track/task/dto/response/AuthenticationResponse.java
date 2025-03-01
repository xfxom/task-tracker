package com.track.task.dto.response;

import java.io.Serializable;

public record AuthenticationResponse(String jwt) implements Serializable {

    private static final long serialVersionUID = 1L;

}
