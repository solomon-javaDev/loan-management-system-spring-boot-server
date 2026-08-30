package io.sol.loanmanagementsystemspringbootserver.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UpdateInfo {

    private String latestversion;
    private String downloadUrl;

}
