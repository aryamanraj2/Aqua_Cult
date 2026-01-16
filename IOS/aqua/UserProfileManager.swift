//
//  UserProfileManager.swift
//  aqua
//
//  Created on 02/11/25.
//

import SwiftUI
import Combine

class UserProfileManager: ObservableObject {
    @Published var profile: UserProfile
    
    init(profile: UserProfile = UserProfile.sampleProfile) {
        self.profile = profile
    }
    
    func updateProfile(_ newProfile: UserProfile) {
        profile = newProfile
    }
}