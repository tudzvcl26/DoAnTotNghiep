import { describe, expect, it } from 'vitest'
import { cvTemplates, sampleCv } from './cv.templates'
import { applyCvTemplate, defaultCvDesignConfig, normalizeCvContent } from './cv.types'
import { cvTemplateCategories } from './cv.presets'

describe('original shared CV template library', () => {
  it('has 24 unique presets across all six requested categories', () => {
    expect(cvTemplates).toHaveLength(24)
    expect(new Set(cvTemplates.map(item => item.id)).size).toBe(24)
    expect(new Set(cvTemplates.map(item => item.style))).toEqual(new Set(cvTemplateCategories))
    expect(new Set(cvTemplates.map(item => JSON.stringify(defaultCvDesignConfig(item.id)))).size).toBe(24)
  })
  it.each(cvTemplates)('preserves all content and hidden custom sections applying $name', template => {
    const original = structuredClone(sampleCv)
    original.customSections = [{ id: 'custom-qa', title: 'Ngoại ngữ', visible: false, items: [{ name: 'Tiếng Việt', date: '', description: 'Bản gốc' }] }]
    original.designConfig.sectionVisibility.experience = false
    const before = structuredClone(original)
    const applied = applyCvTemplate(original, template.id)
    const { designConfig: _oldDesign, ...oldContent } = original
    const { designConfig: _newDesign, ...newContent } = applied
    expect(newContent).toEqual(oldContent)
    expect(original).toEqual(before)
    expect(applied.designConfig.sectionVisibility.experience).toBe(false)
    expect(applied.designConfig.sectionOrder).toContain('custom:custom-qa')
    expect(normalizeCvContent(applied, template.id)).toEqual(applied)
  })
})
