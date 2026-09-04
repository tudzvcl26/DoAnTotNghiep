import { expect, it } from 'vitest'
import { employerCompanySchema } from './employer-company.schemas'

it('requires company type and size before a create request reaches the NOT NULL database columns', () => {
  const fields = { name: 'QA company', description: '', website: '', email: '', phone: '', taxCode: '', companyType: '', companySize: '', logoUrl: '', bannerUrl: '' }
  const missing = employerCompanySchema.safeParse(fields)
  expect(missing.success).toBe(false)
  if (!missing.success) expect(missing.error.issues.map(issue => issue.path[0])).toEqual(['companyType', 'companySize'])
  expect(employerCompanySchema.safeParse({ ...fields, companyType: 'PRIVATE', companySize: 'SMALL' }).success).toBe(true)
})
