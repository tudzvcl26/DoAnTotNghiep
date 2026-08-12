import { AiCareerSection } from './components/AiCareerSection'
import { CareerGuideSection } from './components/CareerGuideSection'
import { CareerToolsSection } from './components/CareerToolsSection'
import { CategorySection } from './components/CategorySection'
import { CompanySection } from './components/CompanySection'
import { FeaturedJobsSection } from './components/FeaturedJobsSection'
import { HeroSection } from './components/HeroSection'
import { HomeCtaSection } from './components/HomeCtaSection'
import { LocationSection } from './components/LocationSection'
import './home-page.css'

export function HomePage() {
  return (
    <>
      <HeroSection />
      <FeaturedJobsSection />
      <CategorySection />
      <CompanySection />
      <LocationSection />
      <AiCareerSection />
      <CareerToolsSection />
      <CareerGuideSection />
      <HomeCtaSection />
    </>
  )
}
