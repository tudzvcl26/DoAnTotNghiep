import {
  ChevronDown,
  ChevronUp,
  Copy,
  EyeOff,
  ExternalLink,
  GripVertical,
  Mail,
  MapPin,
  Phone,
  Plus,
  Trash2,
} from "lucide-react";
import type { CSSProperties, ReactNode } from "react";
import type {
  CvCertification,
  CvContent,
  CvCustomSection,
  CvEducation,
  CvExperience,
  CvLanguage,
  CvNamedItem,
  CvProject,
  CvTemplateId,
} from "../cv.types";
import { EditableText } from "./EditableText";

const hasText = (value?: string) => Boolean(value?.trim());
const replaceAt = <T,>(items: T[], index: number, item: T) =>
  items.map((current, currentIndex) =>
    currentIndex === index ? item : current,
  );
const moveAt = <T,>(items: T[], index: number, direction: -1 | 1) => {
  const target = index + direction;
  if (target < 0 || target >= items.length) return items;
  const next = [...items];
  [next[index], next[target]] = [next[target], next[index]];
  return next;
};

const titles: Record<CvLanguage, Record<string, string>> = {
  vi: {
    summary: "Giới thiệu",
    experience: "Kinh nghiệm",
    education: "Học vấn",
    skills: "Kỹ năng",
    projects: "Dự án",
    certifications: "Chứng chỉ",
    awards: "Giải thưởng",
    activities: "Hoạt động",
  },
  en: {
    summary: "Professional summary",
    experience: "Work experience",
    education: "Education",
    skills: "Skills",
    projects: "Projects",
    certifications: "Certifications",
    awards: "Awards",
    activities: "Activities",
  },
};

export type CvPreviewEditor = {
  onChange: (content: CvContent) => void;
  onCheckpoint: () => void;
};
type CvPreviewProps = {
  content: CvContent;
  templateId: CvTemplateId;
  language?: CvLanguage;
  compact?: boolean;
  editor?: CvPreviewEditor;
};
type SectionActions = {
  id: string;
  index: number;
  count: number;
  custom?: CvCustomSection;
};

export function CvPreview({
  content,
  templateId,
  language = "vi",
  compact = false,
  editor,
}: CvPreviewProps) {
  const editing = Boolean(editor) && !compact;
  const personal = content.personalInfo;
  const design = content.designConfig;
  const order = design.sectionOrder;
  const textProps = (label: string) => ({
    label,
    onEditStart: editor?.onCheckpoint,
  });
  const setPersonal = (field: keyof CvContent["personalInfo"], value: string) =>
    editor?.onChange({
      ...content,
      personalInfo: { ...personal, [field]: value },
    });
  const structural = (next: CvContent) => {
    editor?.onCheckpoint();
    editor?.onChange(next);
  };
  const visible = (id: string) =>
    design.sectionVisibility[id] !== false &&
    (id.startsWith("custom:")
      ? content.customSections.find((section) => `custom:${section.id}` === id)
          ?.visible !== false
      : true);
  const moveSection = (id: string, direction: -1 | 1) =>
    structural({
      ...content,
      designConfig: {
        ...design,
        sectionOrder: moveAt(order, order.indexOf(id), direction),
      },
    });
  const hideSection = (id: string) =>
    structural({
      ...content,
      designConfig: {
        ...design,
        sectionVisibility: { ...design.sectionVisibility, [id]: false },
      },
    });
  const deleteCustom = (section: CvCustomSection) =>
    structural({
      ...content,
      customSections: content.customSections.filter(
        (item) => item.id !== section.id,
      ),
      designConfig: {
        ...design,
        sectionOrder: order.filter((id) => id !== `custom:${section.id}`),
      },
    });
  const duplicateCustom = (section: CvCustomSection) => {
    const next = {
      ...section,
      id: crypto.randomUUID(),
      title: `${section.title} (bản sao)`,
      items: structuredClone(section.items),
    };
    const sourceIndex = order.indexOf(`custom:${section.id}`);
    structural({
      ...content,
      customSections: [...content.customSections, next],
      designConfig: {
        ...design,
        sectionOrder: [
          ...order.slice(0, sourceIndex + 1),
          `custom:${next.id}`,
          ...order.slice(sourceIndex + 1),
        ],
      },
    });
  };
  const sectionActions = (
    id: string,
    custom?: CvCustomSection,
  ): SectionActions => ({
    id,
    index: order.indexOf(id),
    count: order.length,
    custom,
  });
  const sectionTitle = (id: string) => titles[language][id];

  const renderSection = (id: string) => {
    if (!visible(id)) return null;
    const actions = sectionActions(id);
    if (id === "summary")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || hasText(content.summary)}
        >
          {editing ? (
            <EditableText
              {...textProps(
                language === "vi"
                  ? "Mục tiêu nghề nghiệp"
                  : "Professional summary",
              )}
              multiline
              value={content.summary}
              placeholder={
                language === "vi"
                  ? "Nhấp để viết mục tiêu nghề nghiệp…"
                  : "Click to write your professional summary…"
              }
              onChange={(summary) => editor?.onChange({ ...content, summary })}
            />
          ) : (
            <p>{content.summary}</p>
          )}
        </CvSection>
      );
    if (id === "experience")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || content.experiences.length > 0}
          onAdd={() =>
            structural({
              ...content,
              experiences: [
                ...content.experiences,
                {
                  position: "",
                  company: "",
                  startDate: "",
                  endDate: "",
                  description: "",
                },
              ],
            })
          }
        >
          {content.experiences.map((item, index) => (
            <EditableExperience
              key={index}
              item={item}
              index={index}
              count={content.experiences.length}
              editing={editing}
              checkpoint={editor?.onCheckpoint}
              onChange={(next) =>
                editor?.onChange({
                  ...content,
                  experiences: replaceAt(content.experiences, index, next),
                })
              }
              onDuplicate={() =>
                structural({
                  ...content,
                  experiences: [
                    ...content.experiences.slice(0, index + 1),
                    { ...item },
                    ...content.experiences.slice(index + 1),
                  ],
                })
              }
              onRemove={() =>
                structural({
                  ...content,
                  experiences: content.experiences.filter(
                    (_, itemIndex) => itemIndex !== index,
                  ),
                })
              }
              onMove={(direction) =>
                structural({
                  ...content,
                  experiences: moveAt(content.experiences, index, direction),
                })
              }
            />
          ))}
        </CvSection>
      );
    if (id === "education")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || content.education.length > 0}
          onAdd={() =>
            structural({
              ...content,
              education: [
                ...content.education,
                {
                  school: "",
                  degree: "",
                  startDate: "",
                  endDate: "",
                  description: "",
                },
              ],
            })
          }
        >
          {content.education.map((item, index) => (
            <EditableEducation
              key={index}
              item={item}
              index={index}
              count={content.education.length}
              editing={editing}
              checkpoint={editor?.onCheckpoint}
              onChange={(next) =>
                editor?.onChange({
                  ...content,
                  education: replaceAt(content.education, index, next),
                })
              }
              onDuplicate={() =>
                structural({
                  ...content,
                  education: [
                    ...content.education.slice(0, index + 1),
                    { ...item },
                    ...content.education.slice(index + 1),
                  ],
                })
              }
              onRemove={() =>
                structural({
                  ...content,
                  education: content.education.filter(
                    (_, itemIndex) => itemIndex !== index,
                  ),
                })
              }
              onMove={(direction) =>
                structural({
                  ...content,
                  education: moveAt(content.education, index, direction),
                })
              }
            />
          ))}
        </CvSection>
      );
    if (id === "skills")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || content.skills.some(hasText)}
          onAdd={() =>
            structural({ ...content, skills: [...content.skills, ""] })
          }
        >
          <div className="cv-paper__skills">
            {content.skills.map((skill, index) =>
              editing ? (
                <div className="cv-inline-skill" key={index}>
                  <EditableText
                    {...textProps(`Kỹ năng ${index + 1}`)}
                    value={skill}
                    placeholder="Kỹ năng mới"
                    onChange={(value) =>
                      editor?.onChange({
                        ...content,
                        skills: replaceAt(content.skills, index, value),
                      })
                    }
                  />
                  <button
                    type="button"
                    aria-label={`Xóa kỹ năng ${index + 1}`}
                    onClick={() =>
                      structural({
                        ...content,
                        skills: content.skills.filter(
                          (_, itemIndex) => itemIndex !== index,
                        ),
                      })
                    }
                  >
                    <Trash2 />
                  </button>
                </div>
              ) : hasText(skill) ? (
                <span key={index}>{skill}</span>
              ) : null,
            )}
          </div>
        </CvSection>
      );
    if (id === "projects")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || content.projects.length > 0}
          onAdd={() =>
            structural({
              ...content,
              projects: [
                ...content.projects,
                { name: "", url: "", description: "" },
              ],
            })
          }
        >
          {content.projects.map((item, index) => (
            <EditableProject
              key={index}
              item={item}
              index={index}
              count={content.projects.length}
              editing={editing}
              checkpoint={editor?.onCheckpoint}
              onChange={(next) =>
                editor?.onChange({
                  ...content,
                  projects: replaceAt(content.projects, index, next),
                })
              }
              onDuplicate={() =>
                structural({
                  ...content,
                  projects: [
                    ...content.projects.slice(0, index + 1),
                    { ...item },
                    ...content.projects.slice(index + 1),
                  ],
                })
              }
              onRemove={() =>
                structural({
                  ...content,
                  projects: content.projects.filter(
                    (_, itemIndex) => itemIndex !== index,
                  ),
                })
              }
              onMove={(direction) =>
                structural({
                  ...content,
                  projects: moveAt(content.projects, index, direction),
                })
              }
            />
          ))}
        </CvSection>
      );
    if (id === "certifications")
      return (
        <CvSection
          key={id}
          title={sectionTitle(id)}
          editable={editing}
          actions={actions}
          onMove={moveSection}
          onHide={hideSection}
          visible={editing || content.certifications.length > 0}
          onAdd={() =>
            structural({
              ...content,
              certifications: [
                ...content.certifications,
                { name: "", issuer: "", date: "" },
              ],
            })
          }
        >
          {content.certifications.map((item, index) => (
            <EditableCertification
              key={index}
              item={item}
              index={index}
              count={content.certifications.length}
              editing={editing}
              checkpoint={editor?.onCheckpoint}
              onChange={(next) =>
                editor?.onChange({
                  ...content,
                  certifications: replaceAt(
                    content.certifications,
                    index,
                    next,
                  ),
                })
              }
              onDuplicate={() =>
                structural({
                  ...content,
                  certifications: [
                    ...content.certifications.slice(0, index + 1),
                    { ...item },
                    ...content.certifications.slice(index + 1),
                  ],
                })
              }
              onRemove={() =>
                structural({
                  ...content,
                  certifications: content.certifications.filter(
                    (_, itemIndex) => itemIndex !== index,
                  ),
                })
              }
              onMove={(direction) =>
                structural({
                  ...content,
                  certifications: moveAt(
                    content.certifications,
                    index,
                    direction,
                  ),
                })
              }
            />
          ))}
        </CvSection>
      );
    if (id === "awards" || id === "activities") {
      const items = content[id];
      return (
        <NamedSection
          key={id}
          title={sectionTitle(id)}
          items={items}
          editing={editing}
          actions={actions}
          checkpoint={editor?.onCheckpoint}
          onMove={moveSection}
          onHide={hideSection}
          onChange={(next) => editor?.onChange({ ...content, [id]: next })}
          onStructural={(next) => structural({ ...content, [id]: next })}
        />
      );
    }
    if (id.startsWith("custom:")) {
      const custom = content.customSections.find(
        (section) => `custom:${section.id}` === id,
      );
      if (!custom) return null;
      return (
        <CustomSection
          key={id}
          section={custom}
          editing={editing}
          actions={sectionActions(id, custom)}
          checkpoint={editor?.onCheckpoint}
          onMove={moveSection}
          onHide={hideSection}
          onDuplicate={() => duplicateCustom(custom)}
          onDelete={() => deleteCustom(custom)}
          onChange={(next) =>
            editor?.onChange({
              ...content,
              customSections: content.customSections.map((section) =>
                section.id === next.id ? next : section,
              ),
            })
          }
          onStructural={(next) =>
            structural({
              ...content,
              customSections: content.customSections.map((section) =>
                section.id === next.id ? next : section,
              ),
            })
          }
        />
      );
    }
    return null;
  };

  const rendered = order.map(renderSection);
  const sidebarIds = new Set(["education", "skills", "certifications"]);
  const sidebar =
    design.layout === "sidebar-left" || design.layout === "sidebar-right";
  const style = {
    "--cv-accent": design.theme.primaryColor,
    "--cv-accent-soft": design.theme.secondaryColor,
    "--cv-text": design.theme.textColor,
    "--cv-muted": design.theme.mutedColor,
    "--cv-background": design.theme.backgroundColor,
    "--cv-font-scale": design.fontScale,
    fontFamily: design.fontFamily,
  } as CSSProperties;

  return (
    <article
      style={style}
      className={`cv-paper cv-paper--${templateId} cv-paper--layout-${design.layout} cv-paper--density-${design.density}${compact ? " cv-paper--compact" : ""}${editing ? " cv-paper--editable" : ""}`}
      aria-label={editing ? "CV đang chỉnh sửa trực tiếp" : "Bản xem trước CV"}
    >
      <header className="cv-paper__header" id="cv-section-personal">
        <h1>
          {editing ? (
            <EditableText
              {...textProps("Họ và tên")}
              value={personal.fullName}
              placeholder="Họ và tên"
              onChange={(value) => setPersonal("fullName", value)}
            />
          ) : (
            personal.fullName || "Họ và tên"
          )}
        </h1>
        <p>
          {editing ? (
            <EditableText
              {...textProps("Chức danh")}
              value={personal.headline}
              placeholder="Vị trí chuyên môn"
              onChange={(value) => setPersonal("headline", value)}
            />
          ) : (
            personal.headline || "Vị trí chuyên môn"
          )}
        </p>
        <div className="cv-paper__contact">
          <ContactField
            icon={<Mail />}
            visible={editing || hasText(personal.email)}
          >
            {editing ? (
              <EditableText
                {...textProps("Email")}
                value={personal.email}
                placeholder="email@example.com"
                onChange={(value) => setPersonal("email", value)}
              />
            ) : (
              personal.email
            )}
          </ContactField>
          <ContactField
            icon={<Phone />}
            visible={editing || hasText(personal.phone)}
          >
            {editing ? (
              <EditableText
                {...textProps("Số điện thoại")}
                value={personal.phone}
                placeholder="Số điện thoại"
                onChange={(value) => setPersonal("phone", value)}
              />
            ) : (
              personal.phone
            )}
          </ContactField>
          <ContactField
            icon={<MapPin />}
            visible={editing || hasText(personal.location)}
          >
            {editing ? (
              <EditableText
                {...textProps("Địa điểm")}
                value={personal.location}
                placeholder="Địa điểm"
                onChange={(value) => setPersonal("location", value)}
              />
            ) : (
              personal.location
            )}
          </ContactField>
          <ContactField
            icon={<ExternalLink />}
            visible={editing || hasText(personal.website)}
          >
            {editing ? (
              <EditableText
                {...textProps("Website")}
                value={personal.website}
                placeholder="Website"
                onChange={(value) => setPersonal("website", value)}
              />
            ) : (
              personal.website
            )}
          </ContactField>
        </div>
      </header>
      {sidebar ? (
        <div className="cv-paper__body-layout">
          <aside>
            {order.map((id, index) =>
              sidebarIds.has(id) ? rendered[index] : null,
            )}
          </aside>
          <div>
            {order.map((id, index) =>
              !sidebarIds.has(id) ? rendered[index] : null,
            )}
          </div>
        </div>
      ) : (
        <div className="cv-paper__body-layout">
          <div>{rendered}</div>
        </div>
      )}
    </article>
  );
}

function ContactField({
  icon,
  visible,
  children,
}: {
  icon: ReactNode;
  visible: boolean;
  children: ReactNode;
}) {
  return visible ? (
    <span>
      {icon}
      {children}
    </span>
  ) : null;
}

function SectionControls({
  actions,
  onMove,
  onHide,
  onDuplicate,
  onDelete,
}: {
  actions: SectionActions;
  onMove: (id: string, direction: -1 | 1) => void;
  onHide: (id: string) => void;
  onDuplicate?: () => void;
  onDelete?: () => void;
}) {
  return (
    <div className="cv-section-controls">
      <span aria-hidden="true" title="Dùng nút lên/xuống để sắp xếp">
        <GripVertical />
      </span>
      <button
        type="button"
        disabled={actions.index <= 0}
        aria-label="Di chuyển section lên"
        onClick={() => onMove(actions.id, -1)}
      >
        <ChevronUp />
      </button>
      <button
        type="button"
        disabled={actions.index >= actions.count - 1}
        aria-label="Di chuyển section xuống"
        onClick={() => onMove(actions.id, 1)}
      >
        <ChevronDown />
      </button>
      {onDuplicate && (
        <button
          type="button"
          aria-label="Nhân bản section"
          onClick={onDuplicate}
        >
          <Copy />
        </button>
      )}
      <button
        type="button"
        aria-label="Ẩn section"
        onClick={() => onHide(actions.id)}
      >
        <EyeOff />
      </button>
      {onDelete && (
        <button type="button" aria-label="Xóa section" onClick={onDelete}>
          <Trash2 />
        </button>
      )}
    </div>
  );
}

function CvSection({
  title,
  visible,
  editable,
  actions,
  onMove,
  onHide,
  onAdd,
  children,
}: {
  title: string;
  visible: boolean;
  editable?: boolean;
  actions: SectionActions;
  onMove: (id: string, direction: -1 | 1) => void;
  onHide: (id: string) => void;
  onAdd?: () => void;
  children: ReactNode;
}) {
  if (!visible) return null;
  return (
    <section
      className={`cv-paper__section${editable ? " is-editable" : ""}`}
      id={`cv-section-${actions.id}`}
    >
      <div className="cv-paper__section-heading">
        <h2>{title}</h2>
        {editable && (
          <SectionControls actions={actions} onMove={onMove} onHide={onHide} />
        )}
        {editable && onAdd && (
          <button
            className="cv-section-add"
            type="button"
            aria-label={`Thêm ${title}`}
            onClick={onAdd}
          >
            <Plus /> Thêm
          </button>
        )}
      </div>
      {children}
    </section>
  );
}

type ItemControlsProps = {
  index: number;
  count: number;
  onDuplicate: () => void;
  onRemove: () => void;
  onMove: (direction: -1 | 1) => void;
};
function ItemControls({
  index,
  count,
  onDuplicate,
  onRemove,
  onMove,
}: ItemControlsProps) {
  return (
    <div className="cv-inline-controls">
      <button
        type="button"
        disabled={index === 0}
        aria-label="Di chuyển lên"
        onClick={() => onMove(-1)}
      >
        <ChevronUp />
      </button>
      <button
        type="button"
        disabled={index === count - 1}
        aria-label="Di chuyển xuống"
        onClick={() => onMove(1)}
      >
        <ChevronDown />
      </button>
      <button type="button" aria-label="Nhân bản mục" onClick={onDuplicate}>
        <Copy />
      </button>
      <button type="button" aria-label="Xóa mục" onClick={onRemove}>
        <Trash2 />
      </button>
    </div>
  );
}

type EditableItemProps<T> = ItemControlsProps & {
  item: T;
  editing: boolean;
  checkpoint?: () => void;
  onChange: (item: T) => void;
};
const fieldProps = (checkpoint: (() => void) | undefined, label: string) => ({
  label,
  onEditStart: checkpoint,
});

function EditableExperience({
  item,
  editing,
  checkpoint,
  onChange,
  ...controls
}: EditableItemProps<CvExperience>) {
  if (!editing)
    return (
      <CvItem
        title={item.position}
        meta={[item.company, formatDates(item.startDate, item.endDate)]
          .filter(Boolean)
          .join(" · ")}
        description={item.description}
      />
    );
  return (
    <div className="cv-paper__item is-editable">
      <ItemControls {...controls} />
      <h3>
        <EditableText
          {...fieldProps(checkpoint, "Vị trí công việc")}
          value={item.position}
          placeholder="Vị trí công việc"
          onChange={(position) => onChange({ ...item, position })}
        />
      </h3>
      <div className="cv-inline-meta">
        <EditableText
          {...fieldProps(checkpoint, "Tên công ty")}
          value={item.company}
          placeholder="Tên công ty"
          onChange={(company) => onChange({ ...item, company })}
        />
        <span>·</span>
        <EditableText
          {...fieldProps(checkpoint, "Thời gian bắt đầu")}
          value={item.startDate}
          placeholder="Bắt đầu"
          onChange={(startDate) => onChange({ ...item, startDate })}
        />
        <span>–</span>
        <EditableText
          {...fieldProps(checkpoint, "Thời gian kết thúc")}
          value={item.endDate}
          placeholder="Hiện tại"
          onChange={(endDate) => onChange({ ...item, endDate })}
        />
      </div>
      <EditableText
        {...fieldProps(checkpoint, "Mô tả kinh nghiệm")}
        multiline
        value={item.description}
        placeholder="Nhấp để mô tả công việc và thành tựu…"
        onChange={(description) => onChange({ ...item, description })}
      />
    </div>
  );
}
function EditableEducation({
  item,
  editing,
  checkpoint,
  onChange,
  ...controls
}: EditableItemProps<CvEducation>) {
  if (!editing)
    return (
      <CvItem
        title={item.degree}
        meta={[item.school, formatDates(item.startDate, item.endDate)]
          .filter(Boolean)
          .join(" · ")}
        description={item.description}
      />
    );
  return (
    <div className="cv-paper__item is-editable">
      <ItemControls {...controls} />
      <h3>
        <EditableText
          {...fieldProps(checkpoint, "Bằng cấp hoặc chuyên ngành")}
          value={item.degree}
          placeholder="Bằng cấp / Chuyên ngành"
          onChange={(degree) => onChange({ ...item, degree })}
        />
      </h3>
      <div className="cv-inline-meta">
        <EditableText
          {...fieldProps(checkpoint, "Tên trường")}
          value={item.school}
          placeholder="Tên trường"
          onChange={(school) => onChange({ ...item, school })}
        />
        <span>·</span>
        <EditableText
          {...fieldProps(checkpoint, "Thời gian bắt đầu học")}
          value={item.startDate}
          placeholder="Bắt đầu"
          onChange={(startDate) => onChange({ ...item, startDate })}
        />
        <span>–</span>
        <EditableText
          {...fieldProps(checkpoint, "Thời gian kết thúc học")}
          value={item.endDate}
          placeholder="Kết thúc"
          onChange={(endDate) => onChange({ ...item, endDate })}
        />
      </div>
      <EditableText
        {...fieldProps(checkpoint, "Mô tả học vấn")}
        multiline
        value={item.description}
        placeholder="Nhấp để thêm GPA, thành tích hoặc môn học nổi bật…"
        onChange={(description) => onChange({ ...item, description })}
      />
    </div>
  );
}
function EditableProject({
  item,
  editing,
  checkpoint,
  onChange,
  ...controls
}: EditableItemProps<CvProject>) {
  if (!editing)
    return (
      <CvItem
        title={item.name}
        meta={item.url}
        description={item.description}
      />
    );
  return (
    <div className="cv-paper__item is-editable">
      <ItemControls {...controls} />
      <h3>
        <EditableText
          {...fieldProps(checkpoint, "Tên dự án")}
          value={item.name}
          placeholder="Tên dự án"
          onChange={(name) => onChange({ ...item, name })}
        />
      </h3>
      <div className="cv-inline-meta">
        <EditableText
          {...fieldProps(checkpoint, "Liên kết dự án")}
          value={item.url}
          placeholder="GitHub / Demo link"
          onChange={(url) => onChange({ ...item, url })}
        />
      </div>
      <EditableText
        {...fieldProps(checkpoint, "Mô tả dự án")}
        multiline
        value={item.description}
        placeholder="Nhấp để mô tả dự án, vai trò và công nghệ…"
        onChange={(description) => onChange({ ...item, description })}
      />
    </div>
  );
}
function EditableCertification({
  item,
  editing,
  checkpoint,
  onChange,
  ...controls
}: EditableItemProps<CvCertification>) {
  if (!editing)
    return (
      <CvItem
        title={item.name}
        meta={[item.issuer, item.date].filter(Boolean).join(" · ")}
      />
    );
  return (
    <div className="cv-paper__item is-editable">
      <ItemControls {...controls} />
      <h3>
        <EditableText
          {...fieldProps(checkpoint, "Tên chứng chỉ")}
          value={item.name}
          placeholder="Tên chứng chỉ"
          onChange={(name) => onChange({ ...item, name })}
        />
      </h3>
      <div className="cv-inline-meta">
        <EditableText
          {...fieldProps(checkpoint, "Đơn vị cấp chứng chỉ")}
          value={item.issuer}
          placeholder="Đơn vị cấp"
          onChange={(issuer) => onChange({ ...item, issuer })}
        />
        <span>·</span>
        <EditableText
          {...fieldProps(checkpoint, "Ngày cấp chứng chỉ")}
          value={item.date}
          placeholder="Thời gian"
          onChange={(date) => onChange({ ...item, date })}
        />
      </div>
    </div>
  );
}

function NamedSection({
  title,
  items,
  editing,
  actions,
  checkpoint,
  onMove,
  onHide,
  onChange,
  onStructural,
}: {
  title: string;
  items: CvNamedItem[];
  editing: boolean;
  actions: SectionActions;
  checkpoint?: () => void;
  onMove: (id: string, direction: -1 | 1) => void;
  onHide: (id: string) => void;
  onChange: (items: CvNamedItem[]) => void;
  onStructural: (items: CvNamedItem[]) => void;
}) {
  return (
    <CvSection
      title={title}
      editable={editing}
      actions={actions}
      onMove={onMove}
      onHide={onHide}
      visible={editing || items.length > 0}
      onAdd={() =>
        onStructural([...items, { name: "", date: "", description: "" }])
      }
    >
      {items.map((item, index) =>
        editing ? (
          <div className="cv-paper__item is-editable" key={index}>
            <ItemControls
              index={index}
              count={items.length}
              onDuplicate={() =>
                onStructural([
                  ...items.slice(0, index + 1),
                  { ...item },
                  ...items.slice(index + 1),
                ])
              }
              onRemove={() =>
                onStructural(
                  items.filter((_, itemIndex) => itemIndex !== index),
                )
              }
              onMove={(direction) =>
                onStructural(moveAt(items, index, direction))
              }
            />
            <h3>
              <EditableText
                {...fieldProps(checkpoint, `Tên ${title.toLowerCase()}`)}
                value={item.name}
                placeholder={`Tên ${title.toLowerCase()}`}
                onChange={(name) =>
                  onChange(replaceAt(items, index, { ...item, name }))
                }
              />
            </h3>
            <div className="cv-inline-meta">
              <EditableText
                {...fieldProps(checkpoint, `Thời gian ${title.toLowerCase()}`)}
                value={item.date}
                placeholder="Thời gian"
                onChange={(date) =>
                  onChange(replaceAt(items, index, { ...item, date }))
                }
              />
            </div>
            <EditableText
              {...fieldProps(checkpoint, `Mô tả ${title.toLowerCase()}`)}
              multiline
              value={item.description}
              placeholder="Nhấp để thêm mô tả…"
              onChange={(description) =>
                onChange(replaceAt(items, index, { ...item, description }))
              }
            />
          </div>
        ) : (
          <CvItem
            key={index}
            title={item.name}
            meta={item.date}
            description={item.description}
          />
        ),
      )}
    </CvSection>
  );
}

function CustomSection({
  section,
  editing,
  actions,
  checkpoint,
  onMove,
  onHide,
  onDuplicate,
  onDelete,
  onChange,
  onStructural,
}: {
  section: CvCustomSection;
  editing: boolean;
  actions: SectionActions;
  checkpoint?: () => void;
  onMove: (id: string, direction: -1 | 1) => void;
  onHide: (id: string) => void;
  onDuplicate: () => void;
  onDelete: () => void;
  onChange: (section: CvCustomSection) => void;
  onStructural: (section: CvCustomSection) => void;
}) {
  return (
    <section
      className={`cv-paper__section${editing ? " is-editable" : ""}`}
      id={`cv-section-custom:${section.id}`}
    >
      <div className="cv-paper__section-heading">
        <h2>
          {editing ? (
            <EditableText
              {...fieldProps(checkpoint, `Tên section ${section.title}`)}
              value={section.title}
              placeholder="Tên mục bổ sung"
              onChange={(title) => onChange({ ...section, title })}
            />
          ) : (
            section.title
          )}
        </h2>
        {editing && (
          <SectionControls
            actions={actions}
            onMove={onMove}
            onHide={onHide}
            onDuplicate={onDuplicate}
            onDelete={onDelete}
          />
        )}
        {editing && (
          <button
            className="cv-section-add"
            type="button"
            aria-label={`Thêm ${section.title}`}
            onClick={() =>
              onStructural({
                ...section,
                items: [
                  ...section.items,
                  { name: "", date: "", description: "" },
                ],
              })
            }
          >
            <Plus /> Thêm
          </button>
        )}
      </div>
      {section.items.map((item, index) =>
        editing ? (
          <div className="cv-paper__item is-editable" key={index}>
            <ItemControls
              index={index}
              count={section.items.length}
              onDuplicate={() =>
                onStructural({
                  ...section,
                  items: [
                    ...section.items.slice(0, index + 1),
                    { ...item },
                    ...section.items.slice(index + 1),
                  ],
                })
              }
              onRemove={() =>
                onStructural({
                  ...section,
                  items: section.items.filter(
                    (_, itemIndex) => itemIndex !== index,
                  ),
                })
              }
              onMove={(direction) =>
                onStructural({
                  ...section,
                  items: moveAt(section.items, index, direction),
                })
              }
            />
            <h3>
              <EditableText
                {...fieldProps(checkpoint, `Tên nội dung ${section.title}`)}
                value={item.name}
                placeholder="Tiêu đề"
                onChange={(name) =>
                  onChange({
                    ...section,
                    items: replaceAt(section.items, index, { ...item, name }),
                  })
                }
              />
            </h3>
            <div className="cv-inline-meta">
              <EditableText
                {...fieldProps(checkpoint, `Thời gian ${section.title}`)}
                value={item.date}
                placeholder="Thời gian / Ghi chú"
                onChange={(date) =>
                  onChange({
                    ...section,
                    items: replaceAt(section.items, index, { ...item, date }),
                  })
                }
              />
            </div>
            <EditableText
              {...fieldProps(checkpoint, `Mô tả ${section.title}`)}
              multiline
              value={item.description}
              placeholder="Nhấp để thêm mô tả…"
              onChange={(description) =>
                onChange({
                  ...section,
                  items: replaceAt(section.items, index, {
                    ...item,
                    description,
                  }),
                })
              }
            />
          </div>
        ) : (
          <CvItem
            key={index}
            title={item.name}
            meta={item.date}
            description={item.description}
          />
        ),
      )}
    </section>
  );
}

function formatDates(start?: string, end?: string) {
  return start && end ? `${start} – ${end}` : start || end || "";
}
function CvItem({
  title,
  meta,
  description,
}: {
  title?: string;
  meta?: string;
  description?: string;
}) {
  return (
    <div className="cv-paper__item">
      <h3>{title || "Nội dung"}</h3>
      {hasText(meta) && <strong>{meta}</strong>}
      {hasText(description) && <p>{description}</p>}
    </div>
  );
}
