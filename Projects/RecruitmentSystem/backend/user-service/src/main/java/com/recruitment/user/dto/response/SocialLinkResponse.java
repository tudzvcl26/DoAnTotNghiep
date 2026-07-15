package com.recruitment.user.dto.response;
import com.recruitment.user.entity.SocialLinkType; import lombok.Builder; import lombok.Getter; import java.util.UUID;
@Getter @Builder public class SocialLinkResponse { private UUID id; private SocialLinkType linkType; private String url; private String label; private Long version; }
